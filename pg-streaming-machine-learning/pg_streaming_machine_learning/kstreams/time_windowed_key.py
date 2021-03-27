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
from dataclasses_avroschema import AvroModel

TIMESTAMP_SIZE = 8


def deserialize_time_windowed_key(windowed_key_bytes: bytes, key_model: AvroModel):
    split_pos = len(windowed_key_bytes) - TIMESTAMP_SIZE

    window_start_bytes = windowed_key_bytes[split_pos:len(windowed_key_bytes)]
    window_start_millis = int.from_bytes(window_start_bytes, 'big')
    window_start_datetime = datetime.fromtimestamp(window_start_millis / 1000)

    key_bytes = windowed_key_bytes[5:split_pos]  # we don't know what the first 5 items are for, KStreams uses all
    key_instance = key_model.deserialize(data=key_bytes)
    return window_start_datetime, key_instance
