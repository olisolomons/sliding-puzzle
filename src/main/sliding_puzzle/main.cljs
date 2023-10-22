(ns sliding-puzzle.main
  "An implementation of https://puzzling.stackexchange.com/questions/122811/sliding-crosses-in-a-5x5-grid"
  (:require
   [quil.core :as q :include-macros true]
   [quil.middleware :as m]
   [quil.sketch :as sketch]))

;; TODO: game scale to fit canvas
;; TODO: win screen
;; TODO: welcome page with play button
;; TODO: level selection
;; TODO: phone touch controls
;; nice to have: image export for your solution
(def margin 20)
(def animation-duration-ms 400)
(def canvas-size 500)

(defn mk-init-state [size crosses]
  {:size size
   :box-width (/ (- canvas-size (* 2 margin)) size)
   :crosses (into {} (map vector crosses (range)))
   :id->pos (into {} (map vector (range) crosses))})

(defn prevent-defaults []
  (.addEventListener
   js/document.body
   "keydown"
   (fn [e]
     (when (= (.-keyCode e) 9)
       (.preventDefault e)))))

(defn- resize-listener [applet]
  (.addEventListener
   js/window
   "resize"
   (fn [_e]
     (let [size (min js/window.innerWidth js/window.innerHeight)]
       (sketch/set-size applet size size)))))

(defn setup []
  (prevent-defaults)
  (resize-listener (sketch/current-applet))
  (q/frame-rate 60)
  (q/color-mode :hsb)
  (mk-init-state
   3
   [[0 0] [1 0] [0 1]]))

(defn update-state [state]
  (let [t (q/millis)
        finished-animations
        (into []
              (comp
               (filter (comp #(> t %) :end-time val))
               (map key))
              (:animations state))]
    (if (seq finished-animations)
      (update state :animations #(apply dissoc % finished-animations))
      state)))

(defn draw-piece [{:keys [box-width selected]} id x y]
  (if (= id selected)
    (q/fill 80 60 200 200)
    (q/fill 80 30 240 200))
  (q/ellipse (* (+ x 0.5) box-width) (* (+ 0.5 y) box-width)
             (- box-width 20) (- box-width 20)))

(defn vector-lerp [[x1 y1] [x2 y2] amount]
  [(q/lerp x1 x2 amount) (q/lerp y1 y2 amount)])

(defn ease-in-out-cubic [x]
  (if (< x 0.5)
    (* 4 x x x)
    (let [x' (+ (* -2 x) 2)]
      (- 1 (* x' x' x' 0.5)))))

(defn draw-state [{:keys [size crosses animations box-width] :as state}]
  (q/background 240)
  (q/no-fill)
  (q/stroke 60)
  (q/stroke-weight 5)
  (q/stroke-join :round)
  (q/with-translation
    [margin margin]
    (let [width (* box-width size)]
      (q/rect 0 0 width width)
      (doseq [i (range (inc size))]
        (q/line (* box-width i) 0 (* box-width i) width))
      (doseq [i (range (inc size))]
        (q/line 0 (* box-width i) width (* box-width i))))

    (doseq [[[x y] id] crosses]
      (if-let [{:keys [from to start-time end-time]}
               (get animations id)]
        (apply draw-piece state id
               (vector-lerp from to
                            (ease-in-out-cubic
                             (/ (- (q/millis) start-time)
                                (- end-time start-time)))))
        (draw-piece state id x y)))))

(defn to-coords [box-width pos]
  (map #(int (/ (- % margin) box-width)) pos))

(defn mouse-pressed [{:keys [crosses box-width] :as state} {:keys [x y button]}]
  (if (= button :left)
    (let [coords (to-coords box-width [x y])]
      (assoc state :selected
             (get crosses coords)))
    state))

(defn vec+ [& vectors]
  (apply mapv + vectors))

(defn in-bounds? [{:keys [size]} pos]
  (every? #(<= 0 % (dec size)) pos))

(defn move [{:keys [selected] :as state} selected-pos direction]
  (let [next-pos (vec+ selected-pos direction)]
    (-> state
        (update :crosses dissoc selected-pos)
        (assoc-in [:crosses next-pos] selected)
        (assoc-in [:id->pos selected] next-pos))))

(defn slide [state selected-pos direction]
  (let [original-pos selected-pos]
    (loop [{:keys [crosses selected] :as state} state
           selected-pos selected-pos]
      (let [next-pos (vec+ selected-pos direction)]
        (if (and (in-bounds? state next-pos) (not (get crosses next-pos)))
          (recur (move state selected-pos direction) next-pos)
          (assoc-in state
                    [:animations selected]
                    {:from original-pos
                     :to selected-pos
                     :start-time (q/millis)
                     :end-time (+ (q/millis) animation-duration-ms)}))))))

(defn key-pressed [{:keys [selected id->pos] :as state} event]
  (if-let [selected-pos (get id->pos selected)]
    (case (:key event)
      (:ArrowRight :d) (slide state selected-pos [1 0])
      (:ArrowLeft :a) (slide state selected-pos [-1 0])
      (:ArrowUp :w) (slide state selected-pos [0 -1])
      (:ArrowDown :s) (slide state selected-pos [0 1])
      :Tab (assoc state :selected
                  (if (get id->pos (inc selected))
                    (inc selected)
                    0))
      state)
    (if (= (:key event) :Tab)
      (assoc state :selected 0)
      state)))

(declare the-sketch)

(defn -main []
  (let [size (min js/window.innerWidth js/window.innerHeight)]
    (q/defsketch the-sketch
      :host "app"
      :size [size size]
      :setup setup
      :update update-state
      :draw draw-state
      :mouse-pressed mouse-pressed
      :key-pressed key-pressed
      :middleware [m/fun-mode])))
