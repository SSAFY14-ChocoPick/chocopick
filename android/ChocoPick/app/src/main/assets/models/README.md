# Local model assets

Large `.litertlm` model files are intentionally excluded from the GitHub repository.

Place the required model files in this directory when running the Android app locally.

Download the model assets from the GitHub release:

https://github.com/SSAFY14-ChocoPick/chocopick/releases/tag/model-assets-v1

The `gemma-3n-E2B-it-int4.litertlm` file is split into two release assets. Reassemble it after downloading both parts:

```sh
cat gemma-3n-E2B-it-int4.litertlm.part-aa gemma-3n-E2B-it-int4.litertlm.part-ab > gemma-3n-E2B-it-int4.litertlm
```
