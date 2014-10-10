(ns try-lwjgl.model.sphere
  (:import [org.lwjgl.opengl GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.glu Sphere])
  (:require [try-lwjgl.display.util :as util]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]))

(defn draw []
  (let [sphere (Sphere.)
        w (physics/world-and-objects)
        ball (:ball w)
        ground (:ground w)
        world (:world w)
        pos (physics/get-position ball)]
    (util/with-pushed-matrix
     (shader/with-program
       (GL11/glTranslatef (pos 0) (pos 1) (pos 2))
       (.setDrawStyle sphere GLU/GLU_SILHOUETTE)
       (GL11/glColor4f 0 0.6 0 1)
       (.draw sphere 1.0 30 30)))))
