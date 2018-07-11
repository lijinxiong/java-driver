/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.osgi;

import org.ops4j.pax.exam.Option;

/**
 * Test that uses a policy from a separate bundle from the core driver to ensure that the driver is
 * able to load that policy via Reflection. To support this, the driver uses <code>
 * DynamicImport-Package: *</code>.
 */
public class OsgiCustomLoadBalancingPolicyIT extends OsgiBaseIT {
  @Override
  public Option[] additionalOptions() {
    return new Option[0];
  }

  @Override
  public String[] sessionOptions() {
    return new String[] {
      "basic.load-balancing-policy.class = com.datastax.oss.driver.api.testinfra.loadbalancing.SortingLoadBalancingPolicy"
    };
  }
}