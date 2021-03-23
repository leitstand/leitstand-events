/*
 * Copyright 2020 RtBrick Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.leitstand.event.queue.jsonb;

import javax.json.bind.adapter.JsonbAdapter;

import io.leitstand.commons.model.Scalar;
import io.leitstand.event.queue.service.TopicName;

public class TopicNameAdapter implements JsonbAdapter<TopicName, String> {

	@Override
	public String adaptToJson(TopicName obj) {
		return Scalar.toString(obj);
	}

	@Override
	public TopicName adaptFromJson(String obj) {
		return TopicName.valueOf(obj);
	}

}
