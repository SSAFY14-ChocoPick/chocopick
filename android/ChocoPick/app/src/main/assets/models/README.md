# Local model assets

Large `.litertlm` model files are tracked with Git LFS.

After cloning the repository, run:

```sh
git lfs pull
```

The `gemma-3n-E2B-it-int4.litertlm` model is stored as split LFS files because the single file is larger than GitHub's current LFS object limit for this repository.

Reassemble it after `git lfs pull`:

```sh
cat gemma-3n-E2B-it-int4.litertlm.part-aa gemma-3n-E2B-it-int4.litertlm.part-ab > gemma-3n-E2B-it-int4.litertlm
```
