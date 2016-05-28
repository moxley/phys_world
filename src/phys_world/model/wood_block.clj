(ns phys-world.model.wood-block
  (:import [org.lwjgl.opengl GL11])
  (:require [phys-world.shader :as shader]
            [phys-world.display.util :as util]
            [phys-world.model.textured-panel :as textured-panel]))

(defn draw-block [draw-panel]

  ;; +z face
  (util/with-pushed-matrix
   (GL11/glTranslatef 0 0 1)
   (draw-panel))

  ;; back
  (util/with-pushed-matrix
   (draw-panel))

  ;; +x side
  (util/with-pushed-matrix
   (GL11/glTranslatef 1 0 0)
   (GL11/glRotatef -90 0 1 0)
   (draw-panel))

  ;; -x side
  (util/with-pushed-matrix
   (GL11/glRotatef -90 0 1 0)
   (GL11/glTranslatef 0 0 0)
   (draw-panel))

  ;; bottom
  (util/with-pushed-matrix
   (GL11/glRotatef 90 1 0 0)
   (draw-panel))

  ;; top
  (util/with-pushed-matrix
   (GL11/glTranslatef 0 1 0)
   (GL11/glRotatef 90 1 0 0)
   (draw-panel))
  )

(defn draw []
  (draw-block textured-panel/draw))
