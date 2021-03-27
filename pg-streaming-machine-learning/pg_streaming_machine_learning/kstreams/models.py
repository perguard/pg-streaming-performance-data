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

from datetime import datetime
from faust import Record
from dataclasses import dataclass
from dataclasses_avroschema import AvroModel, types
from dataclasses_avroschema.types import Enum

WINDOWED_FEATURE_KEY_CODEC = 'windowed_feature_key'
AVRO_FEATURE_KEY_CODEC = 'avro_feature_key'
AVRO_LABELED_FEATURE_CODEC = 'avro_labeled_feature'


class TraceMetric(Enum, AvroModel):
    symbols = ["CPU", "DISK_READ", "DISK_WRITE", "MEMORY", "NETWORK_IN", "NETWORK_OUT", "UNKNOWN"]
    namespace = "pg.trace.model"
    schema_doc = False
    name = "TraceMetric"


class TraceMeasure(Enum, AvroModel):
    symbols = ["UTILIZATION", "THROUGHPUT", "RESPONSE_TIME", "UNKNOWN"]
    namespace = "pg.trace.model"
    schema_doc = False


class TraceUnit(Enum, AvroModel):
    symbols = ["COUNT", "COUNT_PER_SECOND", "BYTES", "BYTES_PER_SECOND", "KILOBYTES", "KILOBYTES_PER_SECOND",
               "MEGABYTES", "MEGABYTES_PER_SECOND", "PERCENT", "MILLISECONDS", "SECONDS", "UNKNOWN"]
    namespace = "pg.trace.model"
    schema_doc = False


@dataclass
class FeatureKey(Record, AvroModel, serializer=AVRO_FEATURE_KEY_CODEC):
    hostname: str
    metric: types.Enum = TraceMetric
    measure: types.Enum = TraceMeasure
    unit: types.Enum = TraceUnit

    class Meta:
        namespace = "pg.streaming.schema"
        schema_doc = False


@dataclass
class TimeWindowedFeatureKey(Record):
    start_time: datetime
    feature_key: FeatureKey


@dataclass
class LabeledFeature(Record, AvroModel, serializer=AVRO_LABELED_FEATURE_CODEC):
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

    class Meta:
        namespace = "pg.streaming.schema"
        schema_doc = False
