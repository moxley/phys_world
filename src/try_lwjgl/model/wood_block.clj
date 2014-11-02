(ns try-lwjgl.model.wood-block
  (:import [org.lwjgl.opengl GL11])
  (:require [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]
            [try-lwjgl.model.textured-panel :as textured-panel]))

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
