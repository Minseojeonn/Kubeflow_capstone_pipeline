You can Install Kubenetes with docker and Kubeflow with this codes.

if you use current PC 
Do noy use 1, 2, 3 script and serching on google. that files are custumed.

after install docker-nvidias

you can use 4,5 scripts 

---------- under code is real workflow when our capstone projects ------------

1. 2,3,4,5번 스크립트 실행 root로하면안됨.
2. 실행된거 확인하면 
3. /home/deep/kubeflow/kubeflow_origin/manifests 에 가서 (없으면 git clone 해야함 https://github.com/kubeflow/manifests 브랜치는 1.6버전 사용) 
4. kustomize build common/cert-manager/cert-manager/base | kubectl apply -f - 실행
5. while ! kustomize build example | kubectl apply -f -; do echo "Retrying to apply resources"; sleep 10; done 실행	
6. 실행되는거 확인 하고
7. kubectl port-forward --address=0.0.0.0 svc/istio-ingressgateway -n istio-system 8080:80 - http 세팅 이전 확인해서 체크하고
9. Kubectl apply -f certificate.yaml 실행
10. Kubectl apply -f gateway.yaml 실행
11. kubectl port-forward --address=0.0.0.0 svc/istio-ingressgateway -n istio-system 8080:443 https 세팅 해주기
12. kubectl edit configmaps -n kserve inferenceservice-config
13. ingressgateway 설정 해줘야함 ingressgateway.png에 있는 것 처럼 하면 됨. 설정하러 가는 명령어는 12번과 같음.

위의 설정들을 통해 https와, ingressgateway의 접근 권한 및 포트포워딩 권한을 취득할 수 있음.
