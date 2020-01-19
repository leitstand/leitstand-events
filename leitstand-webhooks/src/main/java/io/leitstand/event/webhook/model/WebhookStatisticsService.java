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
package io.leitstand.event.webhook.model;

import static io.leitstand.commons.db.DatabaseService.prepare;
import static io.leitstand.event.webhook.service.MessageState.messageState;
import static io.leitstand.event.webhook.service.MessageStateStatistics.newMessageStatistics;
import static io.leitstand.event.webhook.service.WebhookStatistics.newWebhookStatistics;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.leitstand.commons.db.DatabaseService;
import io.leitstand.event.webhook.service.MessageStateStatistics;
import io.leitstand.event.webhook.service.WebhookStatistics;

@Dependent
public class WebhookStatisticsService {

	private DatabaseService db;

	protected WebhookStatisticsService() {
		// CDI
	}
	
	@Inject
	public WebhookStatisticsService(@Webhooks DatabaseService db) {
		this.db = db;
	}

	public WebhookStatistics getWebhookStatistics(Webhook hook) {
		
		List<MessageStateStatistics> stats = db.executeQuery(prepare("SELECT state, min(exectime), cast(avg(exectime) as float), max(exectime), cast(stddev_samp(exectime) AS float), count(*) "+
																	 "FROM bus.webhook_message "+
																	 "WHERE webhook_id = ? "+
																	 "GROUP BY state",
																	 hook.getId()),
															 rs -> newMessageStatistics()
																   .withMessageState(messageState(rs.getString(1)))
																   .withMinExecTime(rs.getObject(2,Integer.class))
																   .withAvgExecTime(rs.getBigDecimal(3))
																   .withMaxExecTime(rs.getObject(4,Integer.class))
																   .withStddevExecTime(rs.getBigDecimal(5))
																   .withMessageCount(rs.getInt(6))
																   .build());
		
		return newWebhookStatistics()
			   .withWebhookId(hook.getWebhookId())
			   .withWebhookName(hook.getWebhookName())
			   .withEnabled(hook.isEnabled())
			   .withDescription(hook.getDescription())
			   .withStatistics(stats)
			   .build();
	}
	
}
