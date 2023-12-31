(ns sliding-puzzle.main
  "An implementation of https://puzzling.stackexchange.com/questions/122811/sliding-crosses-in-a-5x5-grid"
  (:require
   [quil.core :as q :include-macros true]
   [quil.middleware :as m]
   [quil.sketch :as sketch]
   [sliding-puzzle.main-menu :as main-menu]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            mouse-released update-state]]))

;; nice to have: image export for your solution
;; nice to have: level creator
;; TODO: remove brief colour change when swiping a piece

(defn- get-canvas-size []
  (min js/window.innerWidth js/window.innerHeight))

(defn prevent-defaults []
  (.addEventListener
   js/document.body
   "keydown"
   (fn [e]
     (when (= (.-keyCode e) 9)
       ;; stop tab key changing focus
       (.preventDefault e)))))

(defn- resize-listener []
  (let [applet (sketch/current-applet)]
    (.addEventListener
     js/window
     "resize"
     (fn [_e]
       (sketch/with-sketch applet
         (let [canvas-size (get-canvas-size)]
           (q/resize-sketch canvas-size canvas-size)
           (swap! (q/state-atom)
                  (comp add-derived-state
                        #(assoc % :canvas-size canvas-size)))
           ;; magic incantation to stop a weird bug
           ;; where the scene appeared super zoomed-in
           ;; sometimes.
           (q/pixel-density (q/display-density))))))))

(defn- add-touch-listener []
  (let [applet (sketch/current-applet)]
    (.addEventListener
     (-> js/document (.querySelector ".p5Canvas"))
     "touchstart"
     (fn [e]
       (let [touch (aget (.-changedTouches e) 0)
             rect (.getBoundingClientRect (.-target touch))]
         (sketch/with-sketch applet
           (swap! (q/state-atom)
                  (fn [state]
                    (mouse-pressed
                     state
                     {:x (- (.-clientX touch) (.-left rect))
                      :y (- (.-clientY touch) (.-top rect))
                      :button :left})))))
       (.preventDefault e)))
    (.addEventListener
     (-> js/document (.querySelector ".p5Canvas"))
     "touchend"
     (fn [e]
       (let [touch (aget (.-changedTouches e) 0)
             rect (.getBoundingClientRect (.-target touch))]
         (sketch/with-sketch applet
           (swap! (q/state-atom)
                  (fn [state]
                    (mouse-released
                     state
                     {:x (- (.-clientX touch) (.-left rect))
                      :y (- (.-clientY touch) (.-top rect))})))))
       (.preventDefault e)))
    (.addEventListener
     (-> js/document (.querySelector ".p5Canvas"))
     "touchmove"
     (fn [e]
       (let [touch (aget (.-changedTouches e) 0)
             rect (.getBoundingClientRect (.-target touch))]
         (sketch/with-sketch applet
           (swap! (q/state-atom)
                  (fn [state]
                    (mouse-dragged
                     state
                     {:x (- (.-clientX touch) (.-left rect))
                      :y (- (.-clientY touch) (.-top rect))
                      :button :left})))))
       (.preventDefault e)))))

(defn setup []
  (add-touch-listener)
  (prevent-defaults)
  (resize-listener)
  (q/frame-rate 60)
  (q/color-mode :hsb)
  (main-menu/mk-state (get-canvas-size)))

(defn draw-state-wrapped [state]
  (q/background 240)
  (draw-state state))

(declare the-sketch)

(defn -main []
  (let [size (get-canvas-size)]
    (q/defsketch the-sketch
      :host "app"
      :size [size size]
      :setup setup
      :update update-state
      :draw draw-state-wrapped
      :mouse-pressed mouse-pressed
      :mouse-released mouse-released
      :mouse-dragged mouse-dragged
      :key-pressed key-pressed
      :middleware [m/fun-mode])))

