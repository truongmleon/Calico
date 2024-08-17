import itertools

# Define the characters to use
characters = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'

# Generate all 3-letter combinations
all_combinations = [''.join(comb) for comb in itertools.product(characters, repeat=3)]

# Filter out combinations that start or end with '_'
valid_names = [name for name in all_combinations if not (name.startswith('_') or name.endswith('_'))]

# Print the result
print(valid_names)
print(f"Total valid combinations: {len(valid_names)}")
