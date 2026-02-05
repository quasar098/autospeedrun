from PIL.Image import open as open2

font_text = open2("ascii.png", 'r')

with open("glyph_sizes.bin", 'rb') as f:
    sizes = f.read()

check_map = [0] * (127 - 32)
for c in range(32, 127):
    b = sizes[c]
    l, r = b >> 4 & 15, (b & 15) + 1
    x = (c % 16) * 8
    y = (c // 16) * 8
    w = 1
    for dx in range(8):
        if not any(font_text.getpixel((x+dx, y+dy))[3] for dy in range(8)):
            break
        w += 1
    if c == ord('"'):
        w = 4
    rows = []
    for dy in range(8):
        row = []
        for dx in range(w):
            row.append(int(font_text.getpixel((x+dx, y+dy))[3] != 0))
        rows.append(row)
    print(f"{{  // {chr(c).replace(' ', '(space)')}")
    for r in rows:
        print('    ' + str(r).replace("[", "{").replace("]", "}") + ",")
    print("},")
