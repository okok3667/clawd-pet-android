package com.clawd.pet

enum class ClawdAnimation(val assetFile: String) {
    STILL("Clawd-Still.png"),
    WALKING("Clawd-CrabWalking.gif"),
    DANCING("Clawd-Dancing.gif"),
    JUMPING("Clawd-JumpingHappy.gif"),
    LURKING("Clawd-Lurking.gif"),
    WAVING("Clawd-Waving.gif"),
    COOKING("Clawd-Cooking.gif"),
    COOK_INTRO("Clawd-CookIntro.gif"),
    WORKING("Clawd-Working.gif"),
    LOADING("Clawd-Loading.gif")
}

enum class PetBehavior {
    IDLE,
    WALKING_LEFT,
    WALKING_RIGHT,
    DRAGGED,
    FALLING,
    PERFORMING
}
