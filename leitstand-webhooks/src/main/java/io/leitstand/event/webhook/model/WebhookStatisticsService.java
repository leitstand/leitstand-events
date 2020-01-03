/*
 * (c) RtBrick, Inc - All rights reserved, 2015 - 2019
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
