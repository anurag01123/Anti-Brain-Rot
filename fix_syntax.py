import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Fix Stay focused string
content = content.replace('"Stay focused\\nand productive"', '"Stay focused\\nand productive"')
content = content.replace("Stay focused\\nand productive", "Stay focused\\nand productive")
# Actually, the error was: Expecting '"'. Let's just use string replacement carefully.
# Wait, in the script I had:
# Text(
#     "Stay focused\nand productive", 
# I used "Stay focused\\nand productive" which evaluates to "Stay focused\nand productive" in python string, writing literal \n.
# Let's replace the block with correct syntax.
content = re.sub(r'"Stay focused.*?productive",', '"""Stay focused\nand productive""",', content, flags=re.DOTALL)

# Fix Icons.Rounded.Star
content = content.replace("Icons.Rounded.Star", "androidx.compose.material.icons.Icons.Rounded.Star")

# Fix smart cast 1: Add Dialog
old_dialog_img = """                                    if (iconBitmap != null) {
                                        Image(
                                            bitmap = iconBitmap,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {"""
new_dialog_img = """                                    val currentIcon = iconBitmap
                                    if (currentIcon != null) {
                                        Image(
                                            bitmap = currentIcon,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {"""
content = content.replace(old_dialog_img, new_dialog_img)

# Fix smart cast 2: TrackedAppCard
old_card_img = """                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {"""
new_card_img = """                    val currentIcon = iconBitmap
                    if (currentIcon != null) {
                        Image(
                            bitmap = currentIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {"""
content = content.replace(old_card_img, new_card_img)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
