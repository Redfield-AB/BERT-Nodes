import pandas
ids = [[0,0,0]] * len(input_table)
masks = [[0,0,0]] * len(input_table)
segments = [[0,0,0]] * len(input_table)

# Copy input to output
output_table = input_table.copy()
output_table[IDS_COLUMN] = ids
output_table[MASKS_COLUMN] = masks
output_table[SEGMENTS_COLUMN] = segments