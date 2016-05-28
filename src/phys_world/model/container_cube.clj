(ns phys-world.model.container-cube
  (:import [org.lwjgl.opengl GL11])
  (:require [phys-world.shader :as shader]
            [phys-world.display.util :as util]))

(defn draw []
  (shader/with-program
    (util/with-pushed-matrix
     (GL11/glScaled 10.0 10.0 10.0)
     (GL11/glLineWidth 2.5)
     (GL11/glColor3f 1.0 0.0 0.0)

     ;; Back wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 1 -1)
               (GL11/glVertex3f -1 1 -1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 1 -1)
               (GL11/glVertex3f -1 -1 -1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 -1)
               (GL11/glVertex3f 1 -1 -1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 -1 -1)
               (GL11/glVertex3f 1 1 -1))

     ;; Front wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 1 1)
               (GL11/glVertex3f -1 1 1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 1 1)
               (GL11/glVertex3f -1 -1 1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 1)
               (GL11/glVertex3f 1 -1 1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 -1 1)
               (GL11/glVertex3f 1 1 1))

     ;; Left wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 -1)
               (GL11/glVertex3f -1 -1 1)
               (GL11/glVertex3f -1 1 1)
               (GL11/glVertex3f -1 1 -1))

     ;; Right wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 -1 -1)
               (GL11/glVertex3f 1 -1 1)
               (GL11/glVertex3f 1 1 1)
               (GL11/glVertex3f 1 1 -1)))))
