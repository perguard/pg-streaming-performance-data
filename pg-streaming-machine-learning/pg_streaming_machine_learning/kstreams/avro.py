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

from faust.serializers import codecs
from schema_registry.client import SchemaRegistryClient
from schema_registry.serializers import FaustSerializer
from simple_settings import settings

from pg_streaming_machine_learning.kstreams.models import FeatureKey, LabeledFeature, AVRO_FEATURE_KEY_CODEC, \
    AVRO_LABELED_FEATURE_CODEC

client = SchemaRegistryClient(url=settings.SCHEMA_REGISTRY_URL)

avro_feature_key_serializer = FaustSerializer(
    client,
    "pg-streaming-labeled-data_key",
    FeatureKey.avro_schema()
)

avro_labeled_feature_serializer = FaustSerializer(
    client,
    "pg-streaming-labeled-data-value",
    LabeledFeature.avro_schema()
)


def avro_feature_key_codec():
    return avro_feature_key_serializer


def avro_labeled_feature_codec():
    return avro_labeled_feature_serializer


codecs.register(AVRO_FEATURE_KEY_CODEC, avro_feature_key_codec())
codecs.register(AVRO_LABELED_FEATURE_CODEC, avro_labeled_feature_codec())
