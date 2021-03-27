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
from simple_settings import settings
from faust import App, Stream

from pg_streaming_machine_learning.faust.models import SampleKey, SampleValue, map_to_sample_key, map_to_sample_value
from pg_streaming_machine_learning.kstreams.models import LabeledFeature, FeatureKey
from pg_streaming_machine_learning.kstreams.time_windowed_key import deserialize_time_windowed_key
from pg_streaming_machine_learning.skmultiflow.adaptive_random_forest_classifier import PGAdaptiveRandomForestClassifier

logging.basicConfig(level=logging.INFO)
logging.info(f"Using the following settings:"
             f"KAFKA_BOOTSTRAP_SERVER: {settings.KAFKA_BOOTSTRAP_SERVER}, "
             f"SCHEMA_REGISTRY_URL: {settings.SCHEMA_REGISTRY_URL}, "
             f"INPUT_TOPIC: {settings.INPUT_TOPIC}")

adaptive_random_forest_classifier = PGAdaptiveRandomForestClassifier()

app = App(
    version=1,
    autodiscover=True,
    origin='pg_streaming_machine_learning',
    id="1",
    broker=settings.KAFKA_BOOTSTRAP_SERVER
)

labeled_traces_topic = app.topic(
    settings.INPUT_TOPIC,
    partitions=1,
    key_type=bytes,
    value_type=LabeledFeature
)

samples_topic = app.topic(
    "pg-streaming-samples",
    partitions=1,
    key_type=SampleKey,
    value_type=SampleValue
)


@app.agent(labeled_traces_topic)
async def pg_labeled_data(stream: Stream):
    async for event in stream.events():
        start_time, feature_key = deserialize_time_windowed_key(event.key, FeatureKey)
        labeled_feature = event.value
        sample_key = map_to_sample_key(start_time, feature_key)
        sample_value = map_to_sample_value(labeled_feature)
        await pg_samples.send(
            key=sample_key,
            value=sample_value
        )


@app.agent(samples_topic)
async def pg_samples(samples: Stream):
    async for sample in samples.events():
        adaptive_random_forest_classifier.evaluate_sample(sample.key, sample.value)


def main() -> None:
    app.main()
