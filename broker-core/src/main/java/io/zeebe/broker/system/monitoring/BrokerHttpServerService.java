/*
 * Zeebe Broker Core
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.broker.system.monitoring;

import io.zeebe.broker.system.configuration.MetricsCfg;
import io.zeebe.servicecontainer.Service;
import io.zeebe.servicecontainer.ServiceStartContext;
import io.zeebe.servicecontainer.ServiceStopContext;
import io.zeebe.util.metrics.MetricsManager;

public class BrokerHttpServerService implements Service<BrokerHttpServer> {

  private BrokerHttpServer brokerHttpServer;
  private MetricsCfg configuration;
  private BrokerHealthCheckService brokerHealthCheckService;

  public BrokerHttpServerService(
      MetricsCfg cfg, BrokerHealthCheckService brokerHealthCheckService) {
    this.configuration = cfg;
    this.brokerHealthCheckService = brokerHealthCheckService;
  }

  @Override
  public void start(ServiceStartContext startContext) {
    final MetricsManager metricsManager = startContext.getScheduler().getMetricsManager();

    startContext.run(
        () -> {
          brokerHttpServer =
              new BrokerHttpServer(
                  metricsManager,
                  brokerHealthCheckService,
                  configuration.getHost(),
                  configuration.getPort());
        });
  }

  @Override
  public void stop(ServiceStopContext stopContext) {
    stopContext.run(brokerHttpServer::close);
  }

  @Override
  public BrokerHttpServer get() {
    return brokerHttpServer;
  }
}