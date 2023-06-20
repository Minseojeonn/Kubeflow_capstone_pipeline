import kfp
import kfp.components as comp
from kfp import dsl
from kfp import onprem
@dsl.pipeline(
    name='ms_min',
    description='minseo'
)

def create_inference_model():
    kserve_op = comp.load_component_from_url('https://raw.githubusercontent.com/kubeflow/pipelines/'
                                               'master/components/kserve/component.yaml')
    
    isvc_yaml = '''
                apiVersion: "serving.kserve.io/v1beta1"
                kind: "InferenceService"
                metadata:
                    name: "torchserve-temp"
                    namespace: "kubeflow-user-example-com"
                spec:
                    predictor:
                        serviceAccountName: 'sa'
                        pytorch:
                            storageUri: s3://efficientcluster
                            resources:
                                limits:
                                    cpu: "5"
                '''
    
    return kserve_op(action="apply",
              inferenceservice_yaml=isvc_yaml
              )    

def ms_min_pipeline():

    start = dsl.ContainerOp(
        name="Start",
        command=["/bin/sh"],
        image="python:3.9"
       
    )
    
    end = dsl.ContainerOp(
        name="END",
        command=["/bin/sh"],
        image="python:3.9"
    )


    add_p = dsl.ContainerOp(
        name="load_data",
        image="raichal2000/capstonepipe:4",
        command=["python","1_videos_to_img_with_extract_face_and_crop.py"],
        arguments=[
            '--video','/data/src', '--output', '/data/faces'
        ]
    ).apply(onprem.mount_pvc("data", volume_name="data", volume_mount_path="/data"))

    ml = dsl.ContainerOp(
        name="training pipeline",
        image="raichal2000/capstonepipe:8",
        command=["python","2_train_efficient_vit.py"],
        arguments=[
            '--path', '/data/faces', '--savepath', '/train/pth_saves'
        ]
    ).set_gpu_limit(1).apply(onprem.mount_pvc("data", volume_name="data", volume_mount_path="/data")).apply(onprem.mount_pvc("train", volume_name="train", volume_mount_path="/train"))

    mar = dsl.ContainerOp(
        name="Creating Marfile",
        command=["/bin/sh"],
        image="python:3.9",
        arguments=[
            "-c",
            "cd /mar/efficient_vit_mar; pip install torchserve torch-model-archiver torch-workflow-archiver; torch-model-archiver --model-name efficient --version 1.0 --serialized-file /train/efficient_vit.pth --extra-files ./src/efficient_vit.py,./src/architecture.yaml,./src/temp2.png,./src/utils.py,./src/tester.py,./src/albu.py  --handler ./src/handler.py --requirements-file ./src/requirements.txt"
        ],  # pip install => create mar file => make model_store folder => mv marfile to model_store
    ).apply(onprem.mount_pvc("mar", volume_name="mar", volume_mount_path="/mar")).apply(onprem.mount_pvc("train", volume_name="train", volume_mount_path="/train"))
    
    add_p.after(start)
    ml.after(add_p)
    mar.after(ml)
    inference_model = create_inference_model()
    inference_model.apply(onprem.mount_pvc(pvc_name="mar", volume_name="mar", volume_mount_path="/mar"))
    inference_model.after(mar)
    end.after(inference_model)
    
    
if __name__ == "__main__":
    import kfp.compiler as compiler
    compiler.Compiler().compile(ms_min_pipeline, __file__ + ".tar.gz")