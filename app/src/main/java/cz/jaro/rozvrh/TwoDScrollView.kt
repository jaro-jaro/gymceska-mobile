package cz.jaro.rozvrh

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.view.ViewDebug.ExportedProperty
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Scroller
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/*
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */



/**
 *
 * Layout container for a view hierarchy that can be scrolled by the user, allowing it to be larger than the
 * physical display. A TwoDScrollView is a [FrameLayout], meaning you should place one child in it containing
 * the entire contents to scroll; this child may itself be a layout manager with a complex hierarchy of objects. A
 * child that is often used is a [LinearLayout] in a vertical orientation, presenting a vertical array of
 * top-level items that the user can scroll through.
 *
 *
 * The [TextView] class also takes care of its own scrolling, so does not require a TwoDScrollView, but
 * using the two together is possible to achieve the effect of a text view within a larger container.
 *
 * Original source code from Matt Clark at: http://web.archive.org/web/20110625064025/http://blog.gorges.us/2010/06/android-two-dimensional-scrollview
 *
 */
open class TwoDScrollView : FrameLayout {
    private val tempRect = Rect()
    private var lastScroll: Long = 0
    private var scroller: Scroller? = null

    /**
     * When set to true, the scroll view measure its child to make it fill the currently visible
     * area.
     */
    @ExportedProperty(category = "layout")
    private var fillViewport = false

    /**
     * Flag to indicate that we are moving focus ourselves. This is so the code that watches for
     * focus changes initiated outside this TwoDScrollView knows that it does not have to do
     * anything.
     */
    private var twoDScrollViewMovedFocus = false

    /**
     * Position of the last motion event.
     */
    private var lastMotionY = 0f
    private var lastMotionX = 0f

    /**
     * True when the layout has changed but the traversal has not come through yet. Ideally the view
     * hierarchy would keep track of this for us.
     */
    private var isLayoutDirty = true

    /**
     * The child to give focus to in the event that a child has requested focus while the layout is
     * dirty. This prevents the scroll from being wrong if the child has not been laid out before
     * requesting focus.
     */
    private var childToScrollTo: View? = null

    /**
     * True if the user is currently dragging this TwoDScrollView around. This is not the same as
     * 'is being flinged', which can be checked by scroller.isFinished() (flinging begins when the
     * user lifts his finger).
     */
    private var isBeingDragged = false

    /**
     * Determines speed during touch scrolling
     */
    private var velocityTracker: VelocityTracker? = null

    /**
     * Whether arrow scrolling is animated.
     */
    private var touchSlop = 0
    private var minimumVelocity = 0
    private var maximumVelocity = 0

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        initTwoDScrollView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initTwoDScrollView()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!fillViewport) {
            return
        }
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            return
        }
        if (childCount > 0) {
            val child = getChildAt(0)
            var height = measuredHeight
            if (child.measuredHeight < height) {
                val lp = child.layoutParams as LayoutParams
                val childWidthMeasureSpec = getChildMeasureSpec(
                    widthMeasureSpec, paddingLeft
                            + paddingRight, lp.width
                )
                height -= paddingTop
                height -= paddingBottom
                val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    height,
                    MeasureSpec.EXACTLY
                )
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        isLayoutDirty = false
        // Give a child focus if it needs it
        if (childToScrollTo != null && isViewDescendantOf(childToScrollTo!!, this)) {
            scrollToChild(childToScrollTo!!)
        }
        childToScrollTo = null

        // Calling this with the present values causes it to re-clam them
        scrollTo(scrollX, scrollY)
    }

    /**
     * @return The maximum amount this scroll view will scroll in response to an arrow event.
     */
    private val maxScrollAmountVertical: Int
        get() = (MAX_SCROLL_FACTOR * height).toInt()
    private val maxScrollAmountHorizontal: Int
        get() = (MAX_SCROLL_FACTOR * width).toInt()

    private fun initTwoDScrollView() {
        scroller = Scroller(context)
        isFocusable = true
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        setWillNotDraw(false)
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        minimumVelocity = configuration.scaledMinimumFlingVelocity
        maximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    /**
     * @return Returns true this TwoDScrollView can be scrolled
     */
    private fun canScroll(): Boolean {
        val child = getChildAt(0)
        if (child != null) {
            val childHeight = child.height
            val childWidth = child.width
            return (height < childHeight + paddingTop + paddingBottom
                    || width < childWidth + paddingLeft + paddingRight)
        }
        return false
    }

    /**
     * You can call this function yourself to have the scroll view perform scrolling from a key
     * event, just as if the event had been dispatched to it by the view hierarchy.
     *
     * @param event
     * The key event to execute.
     * @return Return true if the event was handled, else false.
     */
    private fun executeKeyEvent(event: KeyEvent): Boolean {
        tempRect.setEmpty()
        if (!canScroll()) {
            if (isFocused) {
                var currentFocused = findFocus()
                if (currentFocused === this) {
                    currentFocused = null
                }
                val nextFocused = FocusFinder.getInstance().findNextFocus(
                    this, currentFocused,
                    FOCUS_DOWN
                )
                return nextFocused != null && nextFocused !== this && nextFocused.requestFocus(FOCUS_DOWN)
            }
            return false
        }
        var handled = false
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> handled = if (!event.isAltPressed) {
                    arrowScroll(FOCUS_UP, false)
                } else {
                    fullScroll(FOCUS_UP, false)
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> handled = if (!event.isAltPressed) {
                    arrowScroll(FOCUS_DOWN, false)
                } else {
                    fullScroll(FOCUS_DOWN, false)
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> handled = if (!event.isAltPressed) {
                    arrowScroll(FOCUS_LEFT, true)
                } else {
                    fullScroll(FOCUS_LEFT, true)
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> handled = if (!event.isAltPressed) {
                    arrowScroll(FOCUS_RIGHT, true)
                } else {
                    fullScroll(FOCUS_RIGHT, true)
                }
            }
        }
        return handled
    }

    private var scaleFactor = 1F

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            scaleFactor = (scaleFactor * detector.scaleFactor).coerceAtMost(5.0F).coerceAtLeast(0.1F)

            val child = children.first()

            child.updateLayoutParams {
                height = (child.height * scaleFactor).toInt()
                width = (child.width * scaleFactor).toInt()
            }

            invalidate()
            return true
        }
    }

    //private val scaleDetector = ScaleGestureDetector(context, scaleListener)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN && ev.edgeFlags != 0) {
            // Don't handle edge touches immediately -- they may actually belong to one of our
            // descendants.
            return false
        }
        if (!canScroll()) {
            return false
        }
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }
        velocityTracker!!.addMovement(ev)
        val action = ev.action
        val y = ev.y
        val x = ev.x
        when (action) {
            MotionEvent.ACTION_DOWN -> {

                // If being flinged and user touches, stop the fling.
                // isFinished will be false if being flinged.
                if (!scroller!!.isFinished) {
                    scroller!!.abortAnimation()
                }

                // Remember where the motion event started
                lastMotionY = y
                lastMotionX = x
            }
            MotionEvent.ACTION_MOVE -> {
                // Scroll to follow the motion event
                var deltaX = (lastMotionX - x).toInt()
                var deltaY = (lastMotionY - y).toInt()
                lastMotionX = x
                lastMotionY = y
                if (deltaX < 0) {
                    if (scrollX < 0) {
                        deltaX = 0
                    }
                } else if (deltaX > 0) {
                    val rightEdge = width - paddingRight
                    val availableToScroll = getChildAt(0).right - scrollX - rightEdge
                    deltaX = if (availableToScroll > 0) {
                        min(availableToScroll, deltaX)
                    } else {
                        0
                    }
                }
                if (deltaY < 0) {
                    if (scrollY < 0) {
                        deltaY = 0
                    }
                } else if (deltaY > 0) {
                    val bottomEdge = height - paddingBottom
                    val availableToScroll = getChildAt(0).bottom - scrollY - bottomEdge
                    deltaY = if (availableToScroll > 0) {
                        min(availableToScroll, deltaY)
                    } else {
                        0
                    }
                }
                if (deltaY != 0 || deltaX != 0) {
                    scrollBy(deltaX, deltaY)
                }
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = velocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val initialXVelocity = velocityTracker.xVelocity.toInt()
                val initialYVelocity = velocityTracker.yVelocity.toInt()
                if (abs(initialXVelocity) + abs(initialYVelocity) > minimumVelocity
                    && childCount > 0
                ) {
                    fling(-initialXVelocity, -initialYVelocity)
                }
                if (this.velocityTracker != null) {
                    this.velocityTracker!!.recycle()
                    this.velocityTracker = null
                }
            }
        }

        //scaleDetector.onTouchEvent(ev)
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val currentFocused = findFocus()
        if (null == currentFocused || this === currentFocused) {
            return
        }

        // If the currently-focused view was visible on the screen when the
        // screen was at the old height, then scroll the screen to make that
        // view visible with the new screen height.
        currentFocused.getDrawingRect(tempRect)
        offsetDescendantRectToMyCoords(currentFocused, tempRect)
        val scrollDeltaX = computeScrollDeltaToGetChildRectOnScreen(tempRect)
        val scrollDeltaY = computeScrollDeltaToGetChildRectOnScreen(tempRect)
        doScroll(scrollDeltaX, scrollDeltaY)
    }

    /**
     * {@inheritDoc}
     *
     * This version also clamps the scrolling to the bounds of our child.
     */
    override fun scrollTo(x: Int, y: Int) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        var x1 = x
        var y1 = y
        if (childCount > 0) {
            val child = getChildAt(0)
            x1 = clamp(x1, width - paddingRight - paddingLeft, child.width)
            y1 = clamp(y1, height - paddingBottom - paddingTop, child.height)
            if (x1 != scrollX || y1 != scrollY) {
                super.scrollTo(x1, y1)
            }
        }
    }

    override fun computeScroll() {
        if (scroller!!.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup. We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            // It's a little odd to call onScrollChanged from inside the drawing.
            //
            // It is, except when you remember that computeScroll() is used to
            // animate scrolling. So unless we want to defer the onScrollChanged()
            // until the end of the animated scrolling, we don't really have a
            // choice here.
            //
            // I agree. The alternative, which I think would be worse, is to post
            // something and tell the subclasses later. This is bad because there
            // will be a window where mScrollX/Y is different from what the app
            // thinks it is.
            //
            val oldX = scrollX
            val oldY = scrollY
            val x = scroller!!.currX
            val y = scroller!!.currY
            if (childCount > 0) {
                val child = getChildAt(0)
                scrollTo(
                    clamp(
                        x, width - paddingRight - paddingLeft,
                        child.width
                    ),
                    clamp(
                        y, height - paddingBottom - paddingTop,
                        child.height
                    )
                )
            } else {
                scrollTo(x, y)
            }
            if (oldX != scrollX || oldY != scrollY) {
                onScrollChanged(scrollX, scrollY, oldX, oldY)
            }

            // Keep on drawing until the animation has finished.
            postInvalidate()
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        if (childCount == 0) {
            return 0.0f
        }
        val length = verticalFadingEdgeLength
        return if (scrollY < length) {
            scrollY / length.toFloat()
        } else 1.0f
    }

    override fun getBottomFadingEdgeStrength(): Float {
        if (childCount == 0) {
            return 0.0f
        }
        val length = verticalFadingEdgeLength
        val bottomEdge = height - paddingBottom
        val span = getChildAt(0).bottom - scrollY - bottomEdge
        return if (span < length) {
            span / length.toFloat()
        } else 1.0f
    }

    override fun getLeftFadingEdgeStrength(): Float {
        if (childCount == 0) {
            return 0.0f
        }
        val length = horizontalFadingEdgeLength
        return if (scrollX < length) {
            scrollX / length.toFloat()
        } else 1.0f
    }

    override fun getRightFadingEdgeStrength(): Float {
        if (childCount == 0) {
            return 0.0f
        }
        val length = horizontalFadingEdgeLength
        val rightEdge = width - paddingRight
        val span = getChildAt(0).right - scrollX - rightEdge
        return if (span < length) {
            span / length.toFloat()
        } else 1.0f
    }

    override fun computeHorizontalScrollRange(): Int {
        val count = childCount
        return if (count == 0) width else getChildAt(0).right
    }

    /**
     *
     * The scroll range of a scroll view is the overall height of all of its children.
     */
    override fun computeVerticalScrollRange(): Int {
        val count = childCount
        return if (count == 0) height else getChildAt(0).bottom
    }

    override fun requestLayout() {
        isLayoutDirty = true
        super.requestLayout()
    }

    /**
     * Finds the next focusable component that fits in this View's bounds (excluding fading edges)
     * pretending that this View's top is located at the parameter top.
     *
     * @param topFocus
     * look for a candidate is the one at the top of the bounds if topFocus is true, or at
     * the bottom of the bounds if topFocus is false
     * @param top
     * the top offset of the bounds in which a focusable must be found (the fading edge is
     * assumed to start at this position)
     * @param preferredFocusable
     * the View that has highest priority and will be returned if it is within my bounds
     * (null is valid)
     * @return the next focusable component in the bounds or null if none can be found
     */
    private fun findFocusableViewInMyBounds(
        topFocus: Boolean, top: Int, leftFocus: Boolean,
        left: Int, preferredFocusable: View?
    ): View? {
        // The fading edge's transparent side should be considered for focus since it's mostly
        // visible, so we divide the actual fading edge length by 2.
        val verticalFadingEdgeLength = verticalFadingEdgeLength / 2
        val topWithoutFadingEdge = top + verticalFadingEdgeLength
        val bottomWithoutFadingEdge = top + height - verticalFadingEdgeLength
        val horizontalFadingEdgeLength = horizontalFadingEdgeLength / 2
        val leftWithoutFadingEdge = left + horizontalFadingEdgeLength
        val rightWithoutFadingEdge = left + width - horizontalFadingEdgeLength
        return if (preferredFocusable != null && preferredFocusable.top < bottomWithoutFadingEdge
            && preferredFocusable.bottom > topWithoutFadingEdge
            && preferredFocusable.left < rightWithoutFadingEdge
            && preferredFocusable.right > leftWithoutFadingEdge
        ) {
            preferredFocusable
        } else findFocusableViewInBounds(
            topFocus, topWithoutFadingEdge, bottomWithoutFadingEdge,
            leftFocus, leftWithoutFadingEdge, rightWithoutFadingEdge
        )
    }

    /**
     * Finds the next focusable component that fits in the specified bounds.
     *
     * @param topFocus
     * look for a candidate is the one at the top of the bounds if topFocus is true, or at
     * the bottom of the bounds if topFocus is false
     * @param top
     * the top offset of the bounds in which a focusable must be found
     * @param bottom
     * the bottom offset of the bounds in which a focusable must be found
     * @return the next focusable component in the bounds or null if none can be found
     */
    private fun findFocusableViewInBounds(
        topFocus: Boolean, top: Int, bottom: Int,
        leftFocus: Boolean, left: Int, right: Int
    ): View? {
        val focusables: List<View> = getFocusables(FOCUS_FORWARD)
        var focusCandidate: View? = null

        // A fully contained focusable is one where its top is below the bound's top, and its bottom
        // is above the bound's bottom. A partially contained focusable is one where some part of it
        // is within the bounds, but it also has some part that is not within bounds. A fully
        // contained focusable is preferred to a partially contained focusable.
        var foundFullyContainedFocusable = false
        val count = focusables.size
        for (i in 0 until count) {
            val view = focusables[i]
            val viewTop = view.top
            val viewBottom = view.bottom
            val viewLeft = view.left
            val viewRight = view.right
            if (top < viewBottom && viewTop < bottom && left < viewRight && viewLeft < right) {
                // the focusable is in the target area, it is a candidate for focusing
                val viewIsFullyContained = (top < viewTop && viewBottom < bottom
                        && left < viewLeft && viewRight < right)
                if (focusCandidate == null) {
                    // No candidate, take this one
                    focusCandidate = view
                    foundFullyContainedFocusable = viewIsFullyContained
                } else {
                    val viewIsCloserToVerticalBoundary = topFocus && viewTop < focusCandidate
                        .top || !topFocus && viewBottom > focusCandidate.bottom
                    val viewIsCloserToHorizontalBoundary = leftFocus && viewLeft < focusCandidate
                        .left ||
                            !leftFocus && viewRight > focusCandidate.right
                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToVerticalBoundary
                            && viewIsCloserToHorizontalBoundary
                        ) {
                            // We're dealing with only fully contained views, so it has to be closer to the
                            // boundary to beat our candidate
                            focusCandidate = view
                        }
                    } else {
                        if (viewIsFullyContained) {
                            // Any fully contained view beats a partially contained view */
                            focusCandidate = view
                            foundFullyContainedFocusable = true
                        } else if (viewIsCloserToVerticalBoundary
                            && viewIsCloserToHorizontalBoundary
                        ) {
                            // Partially contained view beats another partially contained view if it's closer
                            focusCandidate = view
                        }
                    }
                }
            }
        }
        return focusCandidate
    }

    /**
     *
     * Handles scrolling in response to a "home/end" shortcut press. This method will scroll the
     * view to the top or bottom and give the focus to the topmost/bottommost component in the new
     * visible area. If no component is a good candidate for focus, this scrollview reclaims the
     * focus.
     *
     * @param direction
     * the scroll direction: [View.FOCUS_UP] to go the top of the view or
     * [View.FOCUS_DOWN] to go the bottom
     * @return true if the key event is consumed by this method, false otherwise
     */
    private fun fullScroll(direction: Int, horizontal: Boolean): Boolean {
        return if (!horizontal) {
            val down = direction == FOCUS_DOWN
            val height = height
            tempRect.top = 0
            tempRect.bottom = height
            if (down) {
                val count = childCount
                if (count > 0) {
                    val view = getChildAt(count - 1)
                    tempRect.bottom = view.bottom
                    tempRect.top = tempRect.bottom - height
                }
            }
            scrollAndFocus(direction, tempRect.top, tempRect.bottom, 0, 0, 0)
        } else {
            val right = direction == FOCUS_DOWN
            val width = width
            tempRect.left = 0
            tempRect.right = width
            if (right) {
                val count = childCount
                if (count > 0) {
                    val view = getChildAt(count - 1)
                    tempRect.right = view.bottom
                    tempRect.left = tempRect.right - width
                }
            }
            scrollAndFocus(0, 0, 0, direction, tempRect.top, tempRect.bottom)
        }
    }

    /**
     *
     * Scrolls the view to make the area defined by `top` and `bottom`
     * visible. This method attempts to give the focus to a component visible in this area. If no
     * component can be focused in the new visible area, the focus is reclaimed by this scrollview.
     *
     *
     * @param directionY
     * the scroll direction: [View.FOCUS_UP] to go upward [     ][View.FOCUS_DOWN] to downward
     * @param top
     * the top offset of the new area to be made visible
     * @param bottom
     * the bottom offset of the new area to be made visible
     * @return true if the key event is consumed by this method, false otherwise
     */
    private fun scrollAndFocus(
        directionY: Int, top: Int, bottom: Int, directionX: Int, left: Int,
        right: Int
    ): Boolean {
        var handled = true
        val height = height
        val containerTop = scrollY
        val containerBottom = containerTop + height
        val up = directionY == FOCUS_UP
        val width = width
        val containerLeft = scrollX
        val containerRight = containerLeft + width
        val leftwards = directionX == FOCUS_UP
        var newFocused = findFocusableViewInBounds(up, top, bottom, leftwards, left, right)
        if (newFocused == null) {
            newFocused = this
        }
        if (top >= containerTop && bottom <= containerBottom
            || left >= containerLeft && right <= containerRight
        ) {
            handled = false
        } else {
            val deltaY = if (up) top - containerTop else bottom - containerBottom
            val deltaX = if (leftwards) left - containerLeft else right - containerRight
            doScroll(deltaX, deltaY)
        }
        if (newFocused !== findFocus() && newFocused.requestFocus(directionY)) {
            twoDScrollViewMovedFocus = true
            twoDScrollViewMovedFocus = false
        }
        return handled
    }

    /**
     * Handle scrolling in response to an up or down arrow click.
     *
     * @param direction
     * The direction corresponding to the arrow key that was pressed
     * @return True if we consumed the event, false otherwise
     */
    private fun arrowScroll(direction: Int, horizontal: Boolean): Boolean {
        var currentFocused = findFocus()
        if (currentFocused === this) {
            currentFocused = null
        }
        val nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction)
        val maxJump = if (horizontal) maxScrollAmountHorizontal else maxScrollAmountVertical
        if (!horizontal) {
            if (nextFocused != null) {
                nextFocused.getDrawingRect(tempRect)
                offsetDescendantRectToMyCoords(nextFocused, tempRect)
                val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(tempRect)
                doScroll(0, scrollDelta)
                nextFocused.requestFocus(direction)
            } else {
                // no new focus
                var scrollDelta = maxJump
                if (direction == FOCUS_UP && scrollY < scrollDelta) {
                    scrollDelta = scrollY
                } else if (direction == FOCUS_DOWN) {
                    if (childCount > 0) {
                        val daBottom = getChildAt(0).bottom
                        val screenBottom = scrollY + height
                        if (daBottom - screenBottom < maxJump) {
                            scrollDelta = daBottom - screenBottom
                        }
                    }
                }
                if (scrollDelta == 0) {
                    return false
                }
                doScroll(0, if (direction == FOCUS_DOWN) scrollDelta else -scrollDelta)
            }
        } else {
            if (nextFocused != null) {
                nextFocused.getDrawingRect(tempRect)
                offsetDescendantRectToMyCoords(nextFocused, tempRect)
                val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(tempRect)
                doScroll(scrollDelta, 0)
                nextFocused.requestFocus(direction)
            } else {
                // no new focus
                var scrollDelta = maxJump
                if (direction == FOCUS_UP && scrollY < scrollDelta) {
                    scrollDelta = scrollY
                } else if (direction == FOCUS_DOWN) {
                    if (childCount > 0) {
                        val daBottom = getChildAt(0).bottom
                        val screenBottom = scrollY + height
                        if (daBottom - screenBottom < maxJump) {
                            scrollDelta = daBottom - screenBottom
                        }
                    }
                }
                if (scrollDelta == 0) {
                    return false
                }
                doScroll(if (direction == FOCUS_DOWN) scrollDelta else -scrollDelta, 0)
            }
        }
        return true
    }

    /**
     * Smooth scroll by a Y delta
     *
     * @param deltaY
     * the number of pixels to scroll by on the Y axis
     */
    private fun doScroll(deltaX: Int, deltaY: Int) {
        if (deltaX != 0 || deltaY != 0) {
            smoothScrollBy(deltaX, deltaY)
        }
    }

    /**
     * Like [View.scrollBy], but scroll smoothly instead of immediately.
     *
     * @param dx
     * the number of pixels to scroll by on the X axis
     * @param dy
     * the number of pixels to scroll by on the Y axis
     */
    private fun smoothScrollBy(dx: Int, dy: Int) {
        val duration = AnimationUtils.currentAnimationTimeMillis() - lastScroll
        if (duration > ANIMATED_SCROLL_GAP) {
            scroller!!.startScroll(scrollX, scrollY, dx, dy)
            awakenScrollBars(scroller!!.duration)
            invalidate()
        } else {
            if (!scroller!!.isFinished) {
                scroller!!.abortAnimation()
            }
            scrollBy(dx, dy)
        }
        lastScroll = AnimationUtils.currentAnimationTimeMillis()
    }

    /**
     * Scrolls the view to the given child.
     *
     * @param child
     * the View to scroll to
     */
    private fun scrollToChild(child: View) {
        child.getDrawingRect(tempRect)
        /* Offset from child's local coordinates to TwoDScrollView coordinates */offsetDescendantRectToMyCoords(child, tempRect)
        val scrollDelta = computeScrollDeltaToGetChildRectOnScreen(tempRect)
        if (scrollDelta != 0) {
            scrollBy(0, scrollDelta)
        }
    }

    /**
     * If rect is off screen, scroll just enough to get it (or at least the first screen size chunk
     * of it) on screen.
     *
     * @param rect
     * The rectangle.
     * @param immediate
     * True to scroll immediately without animation
     * @return true if scrolling was performed
     */
    private fun scrollToChildRect(rect: Rect, immediate: Boolean): Boolean {
        val delta = computeScrollDeltaToGetChildRectOnScreen(rect)
        val scroll = delta != 0
        if (scroll) {
            if (immediate) {
                scrollBy(0, delta)
            } else {
                smoothScrollBy(0, delta)
            }
        }
        return scroll
    }

    /**
     * Compute the amount to scroll in the Y direction in order to get a rectangle completely on the
     * screen (or, if taller than the screen, at least the first screen size chunk of it).
     *
     * @param rect
     * The rect.
     * @return The scroll delta.
     */
    private fun computeScrollDeltaToGetChildRectOnScreen(rect: Rect): Int {
        if (childCount == 0) {
            return 0
        }
        val height = height
        var screenTop = scrollY
        var screenBottom = screenTop + height
        val fadingEdge = verticalFadingEdgeLength
        // leave room for top fading edge as long as rect isn't at very top
        if (rect.top > 0) {
            screenTop += fadingEdge
        }

        // leave room for bottom fading edge as long as rect isn't at very bottom
        if (rect.bottom < getChildAt(0).height) {
            screenBottom -= fadingEdge
        }
        var scrollYDelta = 0
        if (rect.bottom > screenBottom && rect.top > screenTop) {
            // need to move down to get it in view: move down just enough so
            // that the entire rectangle is in view (or at least the first
            // screen size chunk).
            scrollYDelta += if (rect.height() > height) {
                // just enough to get screen size chunk on
                rect.top - screenTop
            } else {
                // get entire rect at bottom of screen
                rect.bottom - screenBottom
            }

            // make sure we aren't scrolling beyond the end of our content
            val bottom = getChildAt(0).bottom
            val distanceToBottom = bottom - screenBottom
            scrollYDelta = min(scrollYDelta, distanceToBottom)
        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            // need to move up to get it in view: move up just enough so that
            // entire rectangle is in view (or at least the first screen
            // size chunk of it).
            scrollYDelta -= if (rect.height() > height) {
                // screen size chunk
                screenBottom - rect.bottom
            } else {
                // entire rect at top
                screenTop - rect.top
            }

            // make sure we aren't scrolling any further than the top our content
            scrollYDelta = max(scrollYDelta, -scrollY)
        }
        return scrollYDelta
    }

    override fun requestChildFocus(child: View, focused: View) {
        if (!twoDScrollViewMovedFocus) {
            if (!isLayoutDirty) {
                scrollToChild(focused)
            } else {
                // The child may not be laid out yet, we can't compute the scroll yet
                childToScrollTo = focused
            }
        }
        super.requestChildFocus(child, focused)
    }

    override fun requestChildRectangleOnScreen(child: View, rectangle: Rect, immediate: Boolean): Boolean {
        // offset into coordinate space of this scroll view
        rectangle.offset(child.left - child.scrollX, child.top - child.scrollY)
        return scrollToChildRect(rectangle, immediate)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Let the focused view and/or our descendants get the key first
        val handled = super.dispatchKeyEvent(event)
        return handled || executeKeyEvent(event)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        // This method JUST determines whether we want to intercept the motion. If we return true,
        // onMotionEvent will be called and we do the actual scrolling there.

        // Shortcut the most recurring case: the user is in the dragging state and he is moving his
        // finger. We want to intercept this motion.
        val action = ev.action
        if (action == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true
        }
        if (!canScroll()) {
            isBeingDragged = false
            return false
        }
        val y = ev.y
        val x = ev.x
        when (action) {
            MotionEvent.ACTION_MOVE -> {

                // isBeingDragged == false, otherwise the shortcut would have caught it. Check whether
                // the user has moved far enough from his original down touch.

                // Locally do absolute value. lastMotionY is set to the y value of the down event.
                val yDiff = abs(y - lastMotionY).toInt()
                val xDiff = abs(x - lastMotionX).toInt()
                if (yDiff > touchSlop || xDiff > touchSlop) {
                    isBeingDragged = true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                // Remember location of down touch
                lastMotionY = y
                lastMotionX = x

                // If being flinged and user touches the screen, initiate drag; otherwise don't.
                // scroller.isFinished should be false when being flinged.
                isBeingDragged = !scroller!!.isFinished
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->         // Release the drag
                isBeingDragged = false
        }

        // The only time we want to intercept motion events is if we are in the drag mode.
        return isBeingDragged
    }

    /**
     * When looking for focus in children of a scroll view, need to be a little more careful not to
     * give focus to something that is scrolled off screen.
     *
     *
     * This is more expensive than the default [ViewGroup] implementation,
     * otherwise this behavior might have been made the default.
     */
    override fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect): Boolean {
        // convert from forward / backward notation to up / down / left / right
        var direction1 = direction
        if (direction1 == FOCUS_FORWARD) {
            direction1 = FOCUS_DOWN
        } else if (direction1 == FOCUS_BACKWARD) {
            direction1 = FOCUS_UP
        }
        val nextFocus =
            FocusFinder.getInstance()
                .findNextFocusFromRect(this, previouslyFocusedRect, direction1)
        return nextFocus != null && nextFocus.requestFocus(direction1, previouslyFocusedRect)
    }

    override fun addView(child: View) {
        check(childCount <= 0) { "TwoDScrollView can host only one direct child" }
        super.addView(child)
    }

    override fun addView(child: View, index: Int) {
        check(childCount <= 0) { "TwoDScrollView can host only one direct child" }
        super.addView(child, index)
    }

    override fun addView(child: View, params: ViewGroup.LayoutParams) {
        check(childCount <= 0) { "TwoDScrollView can host only one direct child" }
        super.addView(child, params)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        check(childCount <= 0) { "TwoDScrollView can host only one direct child" }
        super.addView(child, index, params)
    }

    override fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val lp = child.layoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, paddingLeft + paddingRight, lp.width)
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun measureChildWithMargins(
        child: View, parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams
        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.leftMargin + lp.rightMargin, MeasureSpec.UNSPECIFIED)
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED)
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    /**
     * Return true if child is an descendant of parent, (or equal to the parent).
     */
    private fun isViewDescendantOf(child: View, parent: View): Boolean {
        if (child === parent) {
            return true
        }
        val theParent = child.parent
        return theParent is ViewGroup && isViewDescendantOf(theParent as View, parent)
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY
     * The initial velocity in the Y direction. Positive numbers mean that the finger/curor
     * is moving down the screen, which means we want to scroll towards the top.
     */
    private fun fling(velocityX: Int, velocityY: Int) {
        if (childCount > 0) {
            val height = height - paddingBottom - paddingTop
            val bottom = getChildAt(0).height
            val width = width - paddingRight - paddingLeft
            val right = getChildAt(0).width
            scroller!!.fling(
                scrollX, scrollY, velocityX, velocityY, 0, right - width, 0,
                bottom - height
            )
            val movingDown = velocityY > 0
            val movingRight = velocityX > 0
            findFocusableViewInMyBounds(
                movingRight, scroller!!.finalX,
                movingDown, scroller!!.finalY, findFocus()
            )
            awakenScrollBars(scroller!!.duration)
            invalidate()
        }
    }

    private fun clamp(n: Int, my: Int, child: Int): Int {
        if (my >= child || n < 0) {
            return 0
        }
        return if (my + n > child) {
            child - my
        } else n
    }

    companion object {
        const val ANIMATED_SCROLL_GAP = 250
        const val MAX_SCROLL_FACTOR = 0.5f
    }
}
