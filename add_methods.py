import pathlib
import sys

fp = pathlib.Path('C:/shree-ai-os/src/main/java/com/shreeai/os/platform/kernels/inference/engine/DefaultInferenceEngine.java')
lines = fp.read_text().split('\n')

# Find the closing brace of the first 'public InferenceResult infer(' method
method_start = None
for i, line in enumerate(lines):
    if 'public InferenceResult infer(' in line and method_start is None:
        method_start = i
        break

if method_start is None:
    print('Could not find infer method')
    sys.exit(1)

brace_count = 0
insert_at = None
for i in range(method_start, len(lines)):
    brace_count += lines[i].count('{') - lines[i].count('}')
    # Only check after we've seen at least one opening brace
    if brace_count == 0 and i > method_start and lines[i].count('}') > 0:
        insert_at = i + 1
        break

if insert_at is None:
    # Alternative: find the closing brace differently
    brace_count = 0
    started = False
    for i in range(method_start, len(lines)):
        opens = lines[i].count('{')
        closes = lines[i].count('}')
        if opens > 0:
            started = True
        brace_count += opens - closes
        if started and brace_count == 0:
            insert_at = i + 1
            break

print(f'Method starts at line {method_start + 1}')
print(f'Closing brace at line {insert_at - 1 if insert_at else "NOT FOUND"}')
print(f'Insert at line {insert_at}')
if insert_at and insert_at < len(lines):
    print(f'Line at insert: [{lines[insert_at]}]')
print(f'Line before insert: [{lines[insert_at - 1]}]' if insert_at else 'N/A')



