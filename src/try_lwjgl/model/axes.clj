(ns try-lwjgl.model.axes
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]))

(def AXIS-WIDTH 0.05)

(defn draw []
  (shader/with-program
    (util/with-pushed-matrix

     ;; x-axis (red)
     (GL11/glColor3f 1.0 0.0 0.0)
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 0 0)
               (GL11/glVertex3f 1 0 0))

     ;; y-axis (green)
     (GL11/glColor3f 0.0 1.0 0.0)
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 0 -1 0)
               (GL11/glVertex3f 0 1 0))

     ;; z-axis (blue)
     (GL11/glColor3f 0.0 0.0 1.0)
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 0 0 -1)
               (GL11/glVertex3f 0 0 1)))))
