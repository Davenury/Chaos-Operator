## Chaos Operator

K8s operator for supervised chaos engineering.

### Supervised Chaos Engineering
By that term we understand chaos engineering without "random" part. Sometimes it's usable to be able to
apply different chaos actions in controlled way.

### Installation
Either by applying single manifests from `crds` directory or by helm. Move into `charts/chaos-operator` directory and
```bash
helm install <release name> .
```
(Work in Progress - having chart deployed in chart registry).
Helm chart will create chaos-operator deployment, as well as needed resources (those can be disabled by modifying `values.yaml` file).
To execute sample scenario, enter the `crds` directory and:
```bash
kubectl apply -f sample.yaml
```

### Scenario definition
Sample definition is like this:
```yaml
apiVersion: lsc.davenury.github.com/v1
kind: Scenario
metadata:
  name: sample
  namespace: chaos-operator
spec:
  phases:
    - duration: PT6S
      actions:
      - namespace: chaos-operator
        resourceType: deployment
        resourceName: sample-deployment
        action: scale
        value: 2
    - duration: PT3S
      actions:
        - namespace: chaos-operator
          resourceType: pod
          resourceName: nginx
          action: delete
    - duration: PT2S
      actions:
        - namespace: chaos-operator
          resourceType: service
          resourceName: my-service
          action: delete
```
`Spec` is composed of `phases`. A `phase` is a set of `actions` applied at the same time throughout the same span of time.
A `phase` is composed of `duration` (ISO-8601 compatible) and `actions` - a list of `action`.
`action` refers to single action performed on cluster, e.g. deletion of a pod or scaling of a deployment. Each action requires:
* namespace - the namespace of the resource
* resourceType - k8s resource type (e.g. deployment, pod, service)
* resourceName - the name of the resource
* action - a verb that describes what should happen (e.g. scale, delete)
Additional param for `scale` action: value - describes the desired value of the scale action.

### Available actions
* delete pod
* delete service
* scale deployment
