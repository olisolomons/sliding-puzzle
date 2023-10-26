(ns sliding-puzzle.sketch-functions)

(defmulti mouse-dragged :type)
(defmulti update-state :type)
(defmulti draw-state :type)
(defmulti mouse-pressed :type)
(defmulti key-pressed :type)
(defmulti add-derived-state :type)

