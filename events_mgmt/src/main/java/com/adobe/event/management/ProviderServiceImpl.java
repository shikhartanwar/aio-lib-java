/*
 * Copyright 2017 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.adobe.event.management;

import static com.adobe.util.Constants.API_MANAGEMENT_URL;

import com.adobe.Workspace;
import com.adobe.event.management.api.ProviderApi;
import com.adobe.event.management.model.Provider;
import com.adobe.event.management.model.ProviderCollection;
import com.adobe.event.management.model.ProviderInputModel;
import com.adobe.util.FeignUtil;
import feign.RequestInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

class ProviderServiceImpl implements ProviderService {

  private final ProviderApi providerApi;
  private final Workspace workspace;

  ProviderServiceImpl(final RequestInterceptor authInterceptor,
      final Workspace workspace, final String url) {
    String apiUrl = StringUtils.isEmpty(url) ? API_MANAGEMENT_URL : url;
    if (authInterceptor == null) {
      throw new IllegalArgumentException("ProviderService is missing a authentication interceptor");
    }
    if (workspace == null) {
      throw new IllegalArgumentException("ProviderService is missing a workspace context");
    }
    workspace.validateWorkspaceContext();
    this.providerApi = FeignUtil.getDefaultBuilder()
        .requestInterceptor(authInterceptor)
        .target(ProviderApi.class, apiUrl);
    this.workspace = workspace;
  }

  @Override
  public List<Provider> getProviders() {
    Optional<ProviderCollection> providers = providerApi
        .findByConsumerOrgId(workspace.getConsumerOrgId());
    if (providers.isPresent()) {
      return providers.get().getProviders();
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public Optional<Provider> findById(final String id) {
    return providerApi.findById(id, true);
  }

  @Override
  public void delete(final String id) {
    providerApi.delete(workspace.getConsumerOrgId(), workspace.getProjectId(),
        workspace.getWorkspaceId(), id);
  }

  @Override
  public Optional<Provider> create(final ProviderInputModel providerInputModel) {
    return providerApi.create(workspace.getConsumerOrgId(), workspace.getProjectId(),
        workspace.getWorkspaceId(), providerInputModel);
  }

  @Override
  public Optional<Provider> update(final String id, final ProviderInputModel providerUpdateModel) {
    return providerApi.update(workspace.getConsumerOrgId(), workspace.getProjectId(),
        workspace.getWorkspaceId(), id, providerUpdateModel);
  }

  @Override
  public Optional<Provider> findBy(final String providerMetadataId,
      final String instanceId) {
    if (StringUtils.isEmpty(providerMetadataId) || StringUtils
        .isEmpty(workspace.getConsumerOrgId())) {
      throw new IllegalArgumentException(
          "You must specify at least a non empty consumerOrgId and providerMetadataId");
    }
    Optional<ProviderCollection> providerCollection = providerApi
        .findBy(workspace.getConsumerOrgId(), providerMetadataId, instanceId);
    if (providerCollection.isPresent()) {
      if (providerCollection.get().getProviders().isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(providerCollection.get().getProviders().get(1));
        // there can only be one by API contract
      }
    } else {
      return Optional.empty();
    }
  }

}