(ns try-lwjgl.display
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.util.glu GLU]
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
   (GL11/glTranslatef 1.5 0.0 -6.0)

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

(def VERTICE-WIDTH 0.05)

(defn draw-vertices []
  (drawing-object

   (GL11/glTranslatef -1.5 0.0 -6.0)

   ;; x-axis
   (GL11/glColor3f 0.0 0.0 1.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f -1.0 0.0 0.0)
             (GL11/glVertex3f  1.0 0.0 0.0)
             (GL11/glVertex3f  1.0 VERTICE-WIDTH 0.0)
             (GL11/glVertex3f -1.0 VERTICE-WIDTH 0.0))

   ;; y-axis
   (GL11/glColor3f 0.0 1.0 0.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f 0.0 -1.0 0.0)
             (GL11/glVertex3f 0.0  1.0 0.0)
             (GL11/glVertex3f 0.0  1.0 VERTICE-WIDTH)
             (GL11/glVertex3f 0.0 -1.0 VERTICE-WIDTH))

   ;; z-axis
   (GL11/glColor3f 1.0 0.0 0.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f 0.0 0.0 -1.0)
             (GL11/glVertex3f 0.0 0.0  1.0)
             (GL11/glVertex3f VERTICE-WIDTH 0.0  1.0)
             (GL11/glVertex3f VERTICE-WIDTH 0.0 -1.0))))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))

  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  (GL11/glLoadIdentity)
  (GLU/gluLookAt (float 0.0) (float 0.0) (float @logic/eye-z)
                 (float 0.0) (float 0.0) (float 0.0)
                 (float 0.0) (float 1.0) (float 0.0))

  (draw-vertices)
  (draw-rectangle))

(defn init []
  (init-window WIDTH HEIGHT "alpha")
  (init-gl))
