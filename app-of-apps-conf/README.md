# App-of-Apps Deployment Guide

This repository contains a **Helm-based App-of-Apps** structure for deploying:

* ArgoCD Projects
* Tools (shared cluster components)
* Services (microservices across multiple namespaces)

The App-of-Apps itself is deployed via **Helm**, which renders ArgoCD `Application` manifests. ArgoCD then recursively deploys all tools and services.

---

## 1. Architecture Overview

```
app-of-apps-conf/
|  ├── master-apps/
|  │    └── templates/
|  │         ├── argocd-projects.yaml
|  │         └── tools.yaml
│  ├── projects/
│  └── tools/
```

---

## 2. Prerequisites

* Kubernetes cluster access
* Helm v3+
* kubectl
* Git repository containing this chart

---

## 3. Install ArgoCD on the Cluster

```bash

helm upgrade --install argocd tools/argocd \
  --namespace argocd \
  --create-namespace
```

Wait until ArgoCD server is ready

---

## 4. Deploy App-of-Apps Using Helm

```bash
helm upgrade --install app-of-apps ./master-apps \
  --namespace argocd \
  --create-namespace
```

This Helm release will:

1. Create the root ArgoCD Application
2. Create ArgoCD Projects
3. Deploy Tools Applications

ArgoCD will take over the recursive deployment.

---


## 5. Access ArgoCD UI

```bash
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Open in browser: `https://localhost:8080`

---

## 7. Updating 
First updating master apps:
1. Edit `master-apps/argocd-projects.yaml` or `master-apps/tools.yaml` 
2. Upgrade Helm release:

 ```bash
helm upgrade app-of-apps ./master-apps -n argocd
```

Second adding or changing tools:
1. Edit `tools/values.yaml` 

ArgoCD will automatically sync changes.


---

## 8. Adding a New master app or Service

1. Create a yaml under `master-apps/templates` 
2. Add the ArgoCD Application Helm template for this new master app
4. Upgrade Helm release:

```bash
helm upgrade app-of-apps ./master-apps -n argocd
```

---

## 9. Uninstall

Remove App-of-Apps:

```bash
helm uninstall app-of-apps -n argocd
```

Remove ArgoCD:

```bash
helm uninstall argocd -n argocd
```

---

**End of Guide**
