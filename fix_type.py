import re

with open("app/src/main/java/com/example/ui/theme/Type.kt", "r") as f:
    content = f.read()

old_family = """val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal)
)"""

new_family = """val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold)
)"""

content = content.replace(old_family, new_family)

with open("app/src/main/java/com/example/ui/theme/Type.kt", "w") as f:
    f.write(content)
