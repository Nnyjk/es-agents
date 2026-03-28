import re

with open('/home/esa-runner/es-agents/agent/internal/app/agent.go', 'r') as f:
    content = f.read()

# Find conflict markers
conflict_start = content.find('<<<<<<< HEAD')
conflict_end = content.find('>>>>>>> origin/main')

if conflict_start == -1 or conflict_end == -1:
    print("No conflict markers found")
    exit(1)

# Find the ======= separator
separator = content.find('=======', conflict_start)

# Extract parts between markers
head_content = content[conflict_start + len('<<<<<<< HEAD'):separator].strip()
main_content = content[separator + len('======='):conflict_end].strip()

# Build replacement - combine both parts
# Remove leading/trailing whitespace and conflict markers from each part
head_lines = [l for l in head_content.split('\n') if l.strip() and not l.strip().startswith('<<<<<<<') and not l.strip().startswith('=======')]
main_lines = [l for l in main_content.split('\n') if l.strip() and not l.strip().startswith('>>>>>>>') and not l.strip().startswith('=======')]

# Combine: head part (resource handling) + main part (EXEC_PLUGIN_TASK)
merged_lines = head_lines + main_lines
merged = '\n'.join(merged_lines)

# Replace conflict region (including markers and surrounding whitespace)
before = content[:conflict_start].rstrip()
after = content[conflict_end:].lstrip()

new_content = before + '\n' + merged + '\n' + after

with open('/home/esa-runner/es-agents/agent/internal/app/agent.go', 'w') as f:
    f.write(new_content)

print("Conflict resolved successfully")
