## 프로젝트 소개

이 프로젝트는 2023-1 전북대학교 캡스톤 디자인 출품을 위해 진행되었으며, 최우수상을 수상하였습니다.<br><br>
프로젝트의 주제는 딥페이크 검출 모델 API 개발입니다.<br>
본 프로젝트는 백엔드 API 서버를 개발하는 것을 목표로 진행하였으며 API 사용 예시를 위한 간단한 클라이언트를 구현하였습니다. <br>
GitHub 구성은 6개의 폴더로 이루어지며 각 폴더는 구현해야 할 기능별로 나누어져 있습니다.<br>
각 폴더별로 README와 COMMAND 파일을 만들어 사용법을 작성 해 두었습니다. <br>

## 역할 분담

전민서 : Kuberflow, Baremetal Infra, ML model, Kserve <br>
이혜인 : Web client, Web server 

📌 Tool & language <br>
<img src="https://img.shields.io/badge/java-F7DF1E?style=flat&logo=java&logoColor=white"/>
<img src="https://img.shields.io/badge/javascript-F7DF1E?style=flat&logo=javascript&logoColor=white"/>
<img src="https://img.shields.io/badge/react-61DAFB?style=flat&logo=react&logoColor=white"/>
<img src="https://img.shields.io/badge/springboot-6DB33F?style=flat&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/pytorch-EE4C2C?style=flat&logo=pytorch&logoColor=white"/><br>
<img src="https://img.shields.io/badge/python-3776AB?style=flat&logo=python&logoColor=white"/>
<img src="https://img.shields.io/badge/kubernetes-326CE5?style=flat&logo=kubernetes&logoColor=white"/>
<img src="https://img.shields.io/badge/kubeflow-326CE5?style=flat&logo=kubeflow&logoColor=white"/><br><br>

## 구현 기능 소개
1. 사용자가 에디터에서 게시글을 작성, 등록 시 이미지만 추출하여 딥페이크 검출
2. 사용자가 동영상 업로드 시 이미지 프레임으로 잘라 영상 딥페이크 검출
3. Kubeflow pipeline 을 통한 데이터 전처리, 학습, 모델 자동 배포
<br>

## 아래는 각 폴더의 설명입니다. <span style="color:red">각 폴더별로 README 가 존재합니다.</span>
### 1. 쿠버네티스 클러스터 구축 및 쿠베플로우 설치(setup_kubenetes_cluster_and_kubeflow)
### 2. 쿠버네티스 파이프 라인을 위한 폴더(Pipeline)
### 3. 쿠버네티스 API서버를 위한 폴더(Kserve_make_mar_file)
### 4. 딥페이크 탐치 추론 모델(custum_trainer)
### 5. 클라이언트측 React (deepfake-api-spring-client) 
### 6. 서버측 자바-스프링(deepfake-api-spring-server)

