(ns try-lwjgl.display
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.vector Vector4f]
           [java.awt Font]
           [org.newdawn.slick Color TrueTypeFont])
  (:require [try-lwjgl.logic :as logic]))

(def WIDTH 800)
(def HEIGHT 600)

(defn init-window
  [width height title]
  (def globals (ref {:width width
                     :height height
                     :title title
                     :angle 0.0
                     :last-time (System/currentTimeMillis)}))
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setVSyncEnabled true)
  (Display/setTitle title)
  (Display/create))

(defn init-gl []
  (GL11/glViewport 0 0 WIDTH HEIGHT)

  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GLU/gluPerspective (float 45.0) ;; fovy
                      (/ (float WIDTH) (float HEIGHT)) ;; aspect
                      (float 0.1)     ;; zNear
                      (float 100.0))  ;; zFar

  (GL11/glShadeModel GL11/GL_SMOOTH)
  (GL11/glClearColor (float 0.0) (float 0.0) (float 0.0) (float 0.0))
  (GL11/glClearDepth (float 1.0))
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (GL11/glDepthFunc GL11/GL_LEQUAL)
  (GL11/glHint GL11/GL_PERSPECTIVE_CORRECTION_HINT GL11/GL_NICEST))

(defmacro do-shape [type & commands]
  `(do
    (GL11/glBegin ~type)
    ~@commands
    (GL11/glEnd)))

(defmacro drawing-object [& commands]
  `(do
     (GL11/glPushMatrix)
     ~@commands
     (GL11/glPopMatrix)))

(defn draw-rectangle []
  (drawing-object
   ;; x, y, z
   (GL11/glTranslatef 1.5 0.0 0.0)

   ;; glRotatef
   ;;   arg0: angle
   ;;   arg1: x axis (left to right)
   ;;   arg2: y axis (down to up)
   ;;   arg3: z axis (front to back)
   (GL11/glRotatef @logic/angle 1.0 0.0 0.0)

   (GL11/glColor3f 0.5 0.5 1.0)

   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f -1.0  1.0 0.0)
             (GL11/glVertex3f  1.0  1.0 0.0)
             (GL11/glVertex3f  1.0 -1.0 0.0)
             (GL11/glVertex3f -1.0 -1.0 0.0))))

(def VERTEXT-WIDTH 0.05)

(defn draw-vertices []
  (drawing-object

   ;;(GL11/glTranslatef -1.5 0.0 0.0)

   (GL11/glScaled 4.0 4.0 4.0)

   ;; x-axis (red)
   (GL11/glColor3f 1.0 0.0 0.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f -1.0 0.0 0.0)
             (GL11/glVertex3f  1.0 0.0 0.0)
             (GL11/glVertex3f  1.0 VERTEXT-WIDTH 0.0)
             (GL11/glVertex3f -1.0 VERTEXT-WIDTH 0.0))

   ;; y-axis (green)
   (GL11/glColor3f 0.0 1.0 0.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f 0.0 -1.0 0.0)
             (GL11/glVertex3f 0.0  1.0 0.0)
             (GL11/glVertex3f 0.0  1.0 VERTEXT-WIDTH)
             (GL11/glVertex3f 0.0 -1.0 VERTEXT-WIDTH))

   ;; z-axis (blue)
   (GL11/glColor3f 0.0 0.0 1.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f 0.0 0.0 -1.0)
             (GL11/glVertex3f 0.0 0.0  1.0)
             (GL11/glVertex3f 0.0 VERTEXT-WIDTH  1.0)
             (GL11/glVertex3f 0.0 VERTEXT-WIDTH -1.0))))

(defn translate-position [position vector]
  (let [position-buf (Vector4f. (float (position 0))
                                (float (position 1))
                                (float (position 2))
                                (float 1))]
    (.translate position-buf
                (float (vector 0))
                (float (vector 1))
                (float (vector 2))
                (float 0))
    [(.getX position-buf)
     (.getY position-buf)
     (.getZ position-buf)
     (float 1)]))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))

  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  (GL11/glLoadIdentity)
  (let [look-pos (translate-position @logic/player-position [0 0 -1])]
    (GLU/gluLookAt (float (@logic/player-position 0)) (float (@logic/player-position 1)) (float (@logic/player-position 2))
                   (look-pos 0) (look-pos 1) (look-pos 2)
                   (float 0.0) (float 1.0) (float 0.0)))

  (draw-vertices)
  (draw-rectangle))

(defn init []
  (init-window WIDTH HEIGHT "alpha")
  (init-gl))
