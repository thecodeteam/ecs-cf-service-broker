package com.emc.ecs.serviceBroker;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.ecs.managementClient.BaseUrlAction;
import com.emc.ecs.managementClient.BucketAclAction;
import com.emc.ecs.managementClient.BucketAction;
import com.emc.ecs.managementClient.BucketQuotaAction;
import com.emc.ecs.managementClient.Connection;
import com.emc.ecs.managementClient.ObjectUserAction;
import com.emc.ecs.managementClient.ObjectUserSecretAction;
import com.emc.ecs.managementClient.model.BaseUrlInfo;
import com.emc.ecs.managementClient.model.BucketAcl;
import com.emc.ecs.managementClient.model.BucketUserAcl;
import com.emc.ecs.managementClient.model.ObjectBucketCreate;
import com.emc.ecs.managementClient.model.ObjectBucketInfo;
import com.emc.ecs.managementClient.model.UserSecretKey;
import com.emc.ecs.serviceBroker.config.BrokerConfig;
import com.emc.ecs.serviceBroker.config.CatalogConfig;
import com.emc.ecs.serviceBroker.model.PlanProxy;
import com.emc.ecs.serviceBroker.model.ServiceDefinitionProxy;

@Service
public class EcsService {

	@Autowired
	private Connection connection;
	
	@Autowired
	private BrokerConfig broker;
	
	@Autowired
	private CatalogConfig catalog;

	public EcsService() throws EcsManagementClientException,
					EcsManagementResourceNotFoundException {
		super();
	}
	
	@PostConstruct
	public void initialize() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		prepareRepository();

		if (broker.getRepositoryEndpoint() == null)
			broker.setRepositoryEndpoint(getObjectEndpoint());
	}
	
	public void prepareRepository() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		String bucketName = broker.getRepositoryBucket();
		String userName = broker.getRepositoryUser();
		if (!bucketExists(bucketName))
			createBucket(bucketName, broker.getRepositoryServiceId(),
					broker.getRepositoryPlanId());

		if (!userExists(userName)) {
			UserSecretKey secretKey = createUser(userName);
			addUserToBucket(bucketName, userName);
			broker.setRepositorySecret(secretKey.getSecretKey());
		} else {
			broker.setRepositorySecret(getUserSecret(userName));
		}
	}

	private String getUserSecret(String id)
			throws EcsManagementClientException {
		return ObjectUserSecretAction.list(connection, prefix(id)).get(0)
				.getSecretKey();
	}

	public ObjectBucketInfo getBucketInfo(String id)
			throws EcsManagementClientException {
		return BucketAction.get(connection, prefix(id), broker.getNamespace());
	}

	public void deleteBucket(String id) throws EcsManagementClientException {
		BucketAction.delete(connection, prefix(id), broker.getNamespace());
	}

	public void createBucket(String id, String serviceId, String planId)
			throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		ServiceDefinitionProxy service = catalog
				.findServiceDefinition(serviceId);
		if (service == null)
			throw new EcsManagementClientException(
					"No service matching service id: " + serviceId);

		PlanProxy plan = service.findPlan(planId);
		if (plan == null)
			throw new EcsManagementClientException(
					"No service matching plan id: " + planId);

		ObjectBucketCreate createParam = new ObjectBucketCreate();
		createParam.setName(prefix(id));
		createParam.setNamespace(broker.getNamespace());
		createParam.setVpool(broker.getReplicationGroup());
		createParam.setHeadType(service.getHeadType());
		createParam.setFilesystemEnabled(service.getFileSystemEnabled());
		createParam.setIsStaleAllowed(service.getStaleAllowed());

		BucketAction.create(connection, createParam);

		int limit = plan.getQuotaLimit();
		int warning = plan.getQuotaWarning();

		// no quota needed if neither is set
		if (limit != -1 || warning != -1)
			BucketQuotaAction.create(connection, prefix(id),
					broker.getNamespace(), limit, warning);
	}

	public void changeBucketPlan(String id, String serviceId, String planId)
			throws EcsManagementClientException {
		ServiceDefinitionProxy service = catalog
				.findServiceDefinition(serviceId);
		if (service == null)
			throw new EcsManagementClientException(
					"No service matching service id: " + serviceId);

		PlanProxy plan = service.findPlan(planId);
		if (plan == null)
			throw new EcsManagementClientException(
					"No service matching plan id: " + planId);

		int limit = plan.getQuotaLimit();
		int warning = plan.getQuotaWarning();
		if (limit == -1 && warning == -1) {
			BucketQuotaAction.delete(connection, prefix(id),
					broker.getNamespace());
		} else {
			BucketQuotaAction.create(connection, prefix(id),
					broker.getNamespace(), limit, warning);
		}
	}

	public UserSecretKey createUser(String id)
			throws EcsManagementClientException {
		ObjectUserAction.create(connection, prefix(id), broker.getNamespace());
		ObjectUserSecretAction.create(connection, prefix(id));
		return ObjectUserSecretAction.list(connection, prefix(id)).get(0);
	}

	public Boolean userExists(String id) throws EcsManagementClientException {
		return ObjectUserAction.exists(connection, prefix(id),
				broker.getNamespace());
	}

	public void deleteUser(String id) throws EcsManagementClientException {
		ObjectUserAction.delete(connection, prefix(id));
	}

	public void addUserToBucket(String id, String username)
			throws EcsManagementClientException {
		BucketAcl acl = BucketAclAction.get(connection, prefix(id),
				broker.getNamespace());
		List<BucketUserAcl> userAcl = acl.getAcl().getUserAccessList();
		userAcl.add(new BucketUserAcl(prefix(username), "full_control"));
		acl.getAcl().setUserAccessList(userAcl);
		BucketAclAction.update(connection, prefix(id), acl);
	}

	public void removeUserFromBucket(String id, String username)
			throws EcsManagementClientException {
		BucketAcl acl = BucketAclAction.get(connection, prefix(id),
				broker.getNamespace());
		List<BucketUserAcl> newUserAcl = acl.getAcl()
				.getUserAccessList()
				.stream()
				.filter(a -> a.getUser().equals(prefix(username)))
				.collect(Collectors.toList());
		acl.getAcl().setUserAccessList(newUserAcl);
		BucketAclAction.update(connection, prefix(id), acl);
	}

	public boolean bucketExists(String id) throws EcsManagementClientException {
		return BucketAction.exists(connection, prefix(id),
				broker.getNamespace());
	}

	public String getObjectEndpoint() throws EcsManagementClientException,
			EcsManagementResourceNotFoundException {
		// with VDC/inactive in the API, we would make a more intelligent
		// selection
		// as it stands, there's not enough info -- just pick the 1st one.
		String id = BaseUrlAction.list(connection).get(0).getId();
		BaseUrlInfo baseUrl = BaseUrlAction.get(connection, id);
		// TODO: switch to TLS end-point and custom S3 trust manager
		return baseUrl.getNamespaceUrl(broker.getNamespace(), false);
	}

	private String prefix(String string) {
		return broker.getPrefix() + string;
	}
}