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

from dataclasses import dataclass
from datetime import datetime
from math import isnan
from faust import Record

from pg_streaming_machine_learning.kstreams.models import FeatureKey, LabeledFeature


@dataclass
class SampleKey(Record, serializer='json'):
    start_time: float
    hostname: str
    metric: str
    measure: str
    unit: str


@dataclass
class SampleValue(Record, serializer='json'):
    minimum: float
    maximum: float
    mean: float
    standard_deviation: float
    percentile25: float
    median: float
    percentile75: float
    skewness: float
    kurtosis: float
    slope: float
    pearsons_correlation: float
    label: int


def map_to_sample_key(start_time: datetime, feature_key: FeatureKey):
    return SampleKey(
        start_time=start_time.timestamp(),
        hostname=feature_key.hostname,
        metric=feature_key.metric,
        measure=feature_key.measure,
        unit=feature_key.unit
    )


def map_to_sample_value(labeled_feature: LabeledFeature):
    return SampleValue(
        minimum=labeled_feature.minimum if not isnan(labeled_feature.minimum) else 0.0,
        maximum=labeled_feature.maximum if not isnan(labeled_feature.maximum) else 0.0,
        mean=labeled_feature.mean if not isnan(labeled_feature.mean) else 0.0,
        standard_deviation=labeled_feature.standard_deviation if not isnan(labeled_feature.standard_deviation) else 0.0,
        percentile25=labeled_feature.percentile25 if not isnan(labeled_feature.percentile25) else 0.0,
        median=labeled_feature.median if not isnan(labeled_feature.median) else 0.0,
        percentile75=labeled_feature.percentile75 if not isnan(labeled_feature.percentile75) else 0.0,
        skewness=labeled_feature.skewness if not isnan(labeled_feature.skewness) else 0.0,
        kurtosis=labeled_feature.kurtosis if not isnan(labeled_feature.kurtosis) else 0.0,
        slope=labeled_feature.slope if not isnan(labeled_feature.slope) else 0.0,
        pearsons_correlation=labeled_feature.pearsons_correlation if not isnan(
            labeled_feature.pearsons_correlation) else 0.0,
        label=labeled_feature.label if not isnan(labeled_feature.label) else 0
    )
