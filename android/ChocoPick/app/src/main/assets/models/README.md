# Local model assets

Large `.litertlm` model files are tracked with Git LFS.

After cloning the repository, run:

```sh
git lfs pull
```

That's all the app needs: the chatbot uses **`gemma3-1b-it-int4.litertlm`** (single file), loaded by `ModelCopier`.

## Optional: `gemma-3n-E2B` (larger model)

`gemma-3n-E2B-it-int4.litertlm` is stored as split LFS files (the single file exceeds GitHub's LFS object limit). It is **not used by default** — reassemble it only if you switch `ModelCopier` to this model:

```sh
cat gemma-3n-E2B-it-int4.litertlm.part-aa gemma-3n-E2B-it-int4.litertlm.part-ab > gemma-3n-E2B-it-int4.litertlm
```
