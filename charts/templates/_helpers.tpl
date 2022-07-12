{{- define "ecs-service-broker.catalog" }}
catalog:
  services:
    -
      name: ecs-bucket
      type: bucket
      id: f3cbab6a-5172-4ff1-a5c7-ab1acadab111
      description: "Elastic Cloud S3 Object Storage Bucket"
      active: true
      bindable: true
      repository-service: true
      metadata:
        displayName: ecs-bucket
        documentationUrl: "https://community.emc.com/docs/DOC-45012"
        imageUrl: "http://www.emc.com/images/products/header-image-icon-ecs.png"
        longDescription: "Dell EMC Elastic Cloud Storage (ECS) Object bucket for storing data using Amazon S3."
        providerDisplayName: "Dell EMC"
        supportUrl: "http://www.emc.com/products-solutions/trial-software-download/ecs.htm"
      plan-updatable: true
      plans:
        - description: "Free Trial"
          free: true
          id: 8e777d49-0a78-4cf4-810a-b5f5173b019d
          metadata:
            bullets:
              - "Shared object storage"
              - "5 GB Storage"
              - "S3 protocol access"
            costs:
              - amount:
                  usd: 0.0
                unit: MONTHLY
          name: 5gb
          service-settings:
            quota:
              limit: 5
              warn: 4
          repository-plan: true
        - description: "Pay per GB for Month"
          id: 89d20694-9ab0-4a98-bc6a-868d6d4ecf31
          metadata:
            bullets:
              - "Shared object storage"
              - "Unlimited Storage"
              - "S3 protocol access"
            costs:
              - amount:
                  usd: 0.03
                unit: "PER GB PER MONTH"
          name: unlimited
      service-settings:
        access-during-outage: true
        ado-read-only: true
        head-type: s3
        service-type: bucket
      tags:
        - s3
        - bucket
    -
      name: ecs-file-bucket
      type: bucket
      id: 9052313c-20b2-47de-900f-6e8b04fafcca
      description: "Elastic Cloud S3 Object Storage File Accessible Bucket"
      active: false
      bindable: true
      repository-service: false
      metadata:
        displayName: ecs-file-bucket
        documentationUrl: "https://community.emc.com/docs/DOC-45012"
        imageUrl: "http://www.emc.com/images/products/header-image-icon-ecs.png"
        longDescription: "Dell EMC Elastic Cloud Storage (ECS) Object bucket for storing data using Amazon S3 or NFS."
        providerDisplayName: "Dell EMC"
        supportUrl: "http://www.emc.com/products-solutions/trial-software-download/ecs.htm"
      plan-updatable: true
      plans:
        -
          description: "Free Trial"
          free: true
          id: b7c3f0b0-6867-44e3-b1b9-b255852010b3
          metadata:
            bullets:
              - "Shared object storage"
              - "5 GB Storage"
              - "S3 protocol and HDFS access"
            costs:
              -
                amount:
                  usd: 0.0
                unit: MONTHLY
          name: 5gb
          service-settings:
            file-accessible: true
            quota:
              limit: 5
              warn: 4
        -
          description: "Pay per GB for Month"
          id: 285817bc-e73d-4bb4-bd5d-aebe54ef792f
          metadata:
            bullets:
              - "Shared object storage"
              - "Unlimited Storage"
              - "S3 protocol and HDFS access"
            costs:
              -
                amount:
                  usd: 0.03
                unit: "PER GB PER MONTH"
          name: unlimited
      requires:
        - volume_mount
      service-settings:
        access-during-outage: true
        file-accessible: true
        head-type: s3
        service-type: bucket
      tags:
        - s3
        - bucket
        - nfs
        - hdfs
    -
      name: ecs-namespace
      type: namespace
      id: 12142a5-5172-4ff1-a5c7-72990f0ce2aa
      description: "Elastic Cloud S3 Object Storage Namespace"
      active: false
      bindable: true
      repository-service: false
      metadata:
        displayName: ecs-namespace
        documentationUrl: "https://community.emc.com/docs/DOC-45012"
        imageUrl: "https://avatars.githubusercontent.com/u/12926680"
        longDescription: "Dell EMC Elastic Cloud Storage (ECS) Object namespace for storing data using Amazon S3."
        providerDisplayName: "Dell EMC"
        supportUrl: "http://www.emc.com/products-solutions/trial-software-download/ecs.htm"
      service-settings:
        access-during-outage: true
        head-type: s3
        service-type: namespace
      plan-updatable: true
      plans:
        - description: "Free Trial"
          free: true
          id: 90f0ce26-0a78-4cf4-810a-b5f5173b019d
          metadata:
            bullets:
              - "Shared object storage"
              - "5 GB Storage"
              - "S3 protocol access"
            costs:
              - amount:
                  usd: 0.0
                unit: MONTHLY
          name: 5gb
          service-settings:
            quota:
              limit: 5
              warn: 4
          repository-plan: true
        - description: "Pay per GB for Month"
          id: 5f5173b0-9ab0-4a98-bc6a-868d6d4ecf31
          metadata:
            bullets:
              - "Shared object storage"
              - "Unlimited Storage"
              - "S3 protocol access"
            costs:
              - amount:
                  usd: 0.03
                unit: "PER GB PER MONTH"
          name: unlimited
      tags:
        - s3
        - namespace
{{- end }}
