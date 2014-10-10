(ns try-lwjgl.model.textured-panel
  (:import [org.lwjgl.opengl GL11]
           [org.newdawn.slick.opengl Texture TextureLoader]
           [org.newdawn.slick.util ResourceLoader]
           [org.newdawn.slick Color])
  (:require [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]))

(def texture (atom nil))

(defn draw []
  (.bind Color/white)
  (.bind @texture)

  (util/do-shape GL11/GL_QUADS
            (GL11/glTexCoord2f 1 0)
            (GL11/glVertex3f 1 0 0)
            (GL11/glTexCoord2f 0 0)
            (GL11/glVertex3f 0 0 0)
            (GL11/glTexCoord2f 0 1)
            (GL11/glVertex3f 0 1 0)
            (GL11/glTexCoord2f 1 1)
            (GL11/glVertex3f 1 1 0)))

(defn setup []
  ;;
  ;; Texture support
  ;;
  (GL11/glEnable GL11/GL_TEXTURE_2D)
  ;; enable alpha blending
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)

  (swap! texture (fn [_]  (TextureLoader/getTexture "JPG" (ResourceLoader/getResourceAsStream "try_lwjgl/mahogany.jpg")))))
