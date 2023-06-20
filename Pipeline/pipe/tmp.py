import kfp
import kfp.components as comp
from kfp import dsl
@dsl.pipeline(
    name='ms_min',
    description='minseo'
)



def ms_min_pipeline():
    
    add_p = dsl.ContainerOp(
        name="load iris data pipeline",
        image="raichal2000/capstonepipe:4",
        command=["python","1_videos_to_img_with_extract_face_and_crop.py"],
        arguments=[
            '--video','./src', '--output', './data'
        ]
    )

    ml = dsl.ContainerOp(
        name="training pipeline",
        image="raichal2000/capstonepipe:5",
        command=["python","2_train_efficient_vit.py"],
        arguments=[
            '--path', 'f{volume}'
        ]
    ).set_gpu_limit(1)

    ml.after(add_p)
    
if __name__ == "__main__":
    import kfp.compiler as compiler
    compiler.Compiler().compile(ms_min_pipeline, __file__ + ".tar.gz")