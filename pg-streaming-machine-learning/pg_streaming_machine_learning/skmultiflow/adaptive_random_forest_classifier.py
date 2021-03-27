#  Copyright (c) 2021 fortiss - Research Institute of the Free State of Bavaria
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Affero General Public License as
#  published by the Free Software Foundation, either version 3 of the
#  License, or (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU Affero General Public License for more details.
#
#  You should have received a copy of the GNU Affero General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.

import logging
import numpy
from skmultiflow.meta import AdaptiveRandomForestClassifier

from pg_streaming_machine_learning.faust.models import SampleKey, SampleValue

logger = logging.getLogger("PGAdaptiveRandomForestClassifier")


class PGAdaptiveRandomForestClassifier:
    """
    AdaptiveRandomForestClassifier based on
    https://scikit-multiflow.readthedocs.io/en/stable/api/generated/skmultiflow.meta.AdaptiveRandomForestClassifier.html


    Methods
    -------
    evaluate_sample(sample_key: pg.faust.models.SampleKey, sample_value: pg.faust.models.SampleValue)
        Predicts and evaluates the model on a sample from a stream.
    """

    def __init__(self):
        self.samples_count = 0
        self.correct_predictions_count = 0
        self.adaptive_random_forest_classifier = AdaptiveRandomForestClassifier(
            nominal_attributes=numpy.array([0, 1, 2, 3])
        )
        self.encoded_hostnames = {}
        self.encoded_metrics = {}
        self.encoded_measures = {}
        self.encoded_units = {}

    def evaluate_sample(self, sample_key: SampleKey, sample_value: SampleValue):
        """
        Predicts and evaluates the model on a sample from a stream.

        Parameters
        ----------
        sample_key : pg.faust.models.SampleKey
            Distinct keys of a feature
        sample_value : pg.faust.models.SampleValue
            Additional values of a feature and the target
        """

        features, targets = self._next_sample(sample_key, sample_value)
        target_predictions = self.adaptive_random_forest_classifier.predict(features)
        if targets[0] == target_predictions[0]:
            self.correct_predictions_count += 1
        self.adaptive_random_forest_classifier.partial_fit(features, targets)
        self.samples_count += 1

        logger.info(f"{self.samples_count} samples analyzed. "
                    f"Accuracy: {self.correct_predictions_count / self.samples_count}")

    def _next_sample(self, sample_key: SampleKey, sample_value: SampleValue) -> (numpy.ndarray, numpy.ndarray):
        feature_array = numpy.array([[
            self._encode_nominal_variable(sample_key.hostname, self.encoded_hostnames),
            self._encode_nominal_variable(sample_key.metric, self.encoded_metrics),
            self._encode_nominal_variable(sample_key.measure, self.encoded_measures),
            self._encode_nominal_variable(sample_key.unit, self.encoded_units),
            sample_value.minimum,
            sample_value.maximum,
            sample_value.mean,
            sample_value.standard_deviation,
            sample_value.percentile25,
            sample_value.median,
            sample_value.percentile75,
            sample_value.skewness,
            sample_value.kurtosis,
            sample_value.slope,
            sample_value.pearsons_correlation
        ]]
        )

        target_array = numpy.ndarray(1)
        target_array[0] = sample_value.label

        return feature_array, target_array

    def _encode_nominal_variable(self, variable: str, encodings: dict) -> float:
        if variable in encodings:
            return encodings[variable]
        else:
            encoding = 1 + len(encodings)
            encodings[variable] = encoding
            return encoding
