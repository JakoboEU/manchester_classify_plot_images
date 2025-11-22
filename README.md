# Classify Plot Images
Use LLM image vision to describe and classify each plot image.
The workflow involves:
* Resize image to reduce input tokens
* Give image to LLM to describe, and classify greenspace
* Add any additional greenspace classifications, e.g. from survey visit, OS greenspace, GM wildlife recording
* Ask LLM to refine greenspace classificaiton given all availbe classifications
* Store [result](./image_classification.json)
