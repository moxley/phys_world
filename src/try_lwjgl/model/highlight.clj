(ns try-lwjgl.model.highlight
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU])
  (:require [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]))

(defn highlight-face [player]
  (shader/with-program
    (util/with-pushed-matrix

      ;; White
      (GL11/glColor3f 0.8 0.8 0.8)
      (util/do-shape GL11/GL_LINES
                     (GL11/glVertex3f 0 0 0)
                     (GL11/glVertex3f 1 0 0)
                     
                     (GL11/glVertex3f 1 0 0)
                     (GL11/glVertex3f 1 1 0)

                     (GL11/glVertex3f 1 1 0)
                     (GL11/glVertex3f 0 1 0)

                     (GL11/glVertex3f 0 1 0)
                     (GL11/glVertex3f 0 0 0)))))
