from PIL import Image

for fname in ["img_1.png", "img_2.png", "img_3.png"]:
    img_1 = Image.open(fname).convert("RGB")
    fn = lambda c: (255, 255, 255) if c == (255, 255, 255) else (0, 0, 0)
    img_1.putdata([fn(px) for px in img_1.get_flattened_data()])
    img_1.save(f"out_{fname}")
