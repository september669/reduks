package org.dda.reduks

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import org.dda.ankoLogger.AnkoLogger
import org.dda.ankoLogger.logDebug
import org.dda.ankoLogger.logError
import org.dda.ankoLogger.logWarn
import org.dda.reduks.coroutines.*


abstract class BaseReduxViewModel<
        State : ReduxState,
        Action : ReduxAction,
        Effect : ReduxSideEffect,
        OneTimeAction : Any,
        >(
    override val dispatchers: CoroutineDispatchers
) : ViewModel(), AnkoLogger, CoroutineExecutionContext {

    abstract val exceptionHandler: ExceptionHandler

    protected abstract val redux: ReduxStore<State, Action, Effect>

    //  ExecutionContext
    private val _isDestroyedContext = atomic(false)
    final override var isDestroyedContext
        get() = _isDestroyedContext.value
        set(value) {
            _isDestroyedContext.value = value
        }

    final override val scope by lazy { CoroutineScope(dispatchers.main) }


    private val _flowShowProgress = MutableSharedFlow<VMEvents.ShowProgress>(replay = 1)
    val flowShowProgress: SharedFlow<VMEvents.ShowProgress> = _flowShowProgress

    private val _flowOneTimeAction = MutableSharedFlow<OneTimeAction>(replay = 0)
    val flowOneTimeAction: SharedFlow<OneTimeAction> = _flowOneTimeAction

    private val _flowState = MutableStateFlow<VMEvents.ViewState<State, ErrorKind>?>(null)
    val flowState: StateFlow<VMEvents.ViewState<State, ErrorKind>?> = _flowState


    final override fun showProgress(show: Boolean, progress: ExecutionProgress) {
        launchUi {
            _flowShowProgress.emit(VMEvents.ShowProgress(show, progress))
        }
    }


    fun showContent(content: VMEvents.ViewState.ShowContent<State>) {
        _flowState.value = content
    }

    fun showError(error: VMEvents.ViewState.ShowError<ErrorKind>) {
        _flowState.value = error
    }

    fun postOneTimeAction(event: OneTimeAction) {
        launchUi {
            logDebug { "postOneTimeAction($event)" }
            _flowOneTimeAction.whenSubscribed {
                emit(event)
            }

        }
    }

    override fun handleException(exc: Throwable): Boolean {
        logError { "handleException(${exc::class.simpleName}: ${exc.message})" }
        val isProcessed = exceptionHandler.handle(exc)
        if (!isProcessed && exc !is CancellationException) {
            logError("Did not handle exception", exc)
        }
        return isProcessed
    }


    override fun onCleared() {
        logDebug("onCleared()")
        super.onCleared()
        cleanExecutionContext()
    }

    /////////////////////

    protected fun initState(block: () -> State) = BuilderState(block)

    protected fun BuilderState<State>.withActions(reducer: ReduxReducer<State, Action>) =
        BuilderReducer(this@withActions, reducer)

    protected fun BuilderReducer<State, Action>.withSideEffects(block: ReduxEffector<State, Effect>): ReduxStore<State, Action, Effect> {
        return BuilderSideEffects(this, block) { newState ->
            showContent(VMEvents.ViewState.ShowContent(newState))
        }.build().also {
            launchUi {
                redux.dispatch()
            }
        }
    }


    fun fire(vararg action: Action) {
        logDebug { "fire(${action.joinToString("; ")})" }
        redux.dispatch(*action)
    }

    infix fun fire(action: Action) {
        logDebug { "fire($action)" }
        redux.dispatch(action)
    }

    infix fun fire(effect: Effect) {
        logDebug { "fireEffect($effect)" }
        redux.dispatchSideEffect(effect)
    }


    open class BuilderState<State : ReduxState>(protected val state: () -> State) {
        constructor(state: BuilderState<State>) : this(state.state)
    }

    open class BuilderReducer<State : ReduxState, Action : ReduxAction>(
        state: BuilderState<State>,
        protected val reducer: ReduxReducer<State, Action>
    ) : BuilderState<State>(state) {
        constructor(reducer: BuilderReducer<State, Action>) : this(
            BuilderState(reducer.state),
            reducer.reducer
        )
    }

    class BuilderSideEffects<State : ReduxState, Action : ReduxAction, Effect : ReduxSideEffect>(
        reducer: BuilderReducer<State, Action>,
        var sideEffects: ReduxEffector<State, Effect>,
        val sendState: (newState: State) -> Unit,
    ) : BuilderReducer<State, Action>(reducer) {
        fun build() = ReduxStore(
            initState = state(),
            dispatcher = DispatcherUi(sendState),
            reducer = reducer,
            sideEffect = sideEffects
        )
    }

    private class DispatcherUi<State : ReduxState>(
        val sendState: (newState: State) -> Unit
    ) :
        Dispatcher<State>(),
        AnkoLogger {

        val isFirstDispatch = atomic(true)

        override fun dispatch(prevState: State, newState: State) {
            if (newState !== prevState || isFirstDispatch.value) {
                isFirstDispatch.value = false
                sendState(newState)
            } else {
                logWarn { "skip dispatch(${prevState.toLogString()}, ${newState.toLogString()})" }
            }
        }
    }

}


sealed class VMEvents {

    data class ShowProgress(
        val show: Boolean,
        val progress: ExecutionProgress = executionProgressGlobalDefault,
    ) : VMEvents()

    sealed class ViewState<out C : ReduxState, out E : ErrorKind> : VMEvents() {

        open class ShowContent<C : ReduxState>(open val content: C) : ViewState<C, Nothing>()
        open class ShowError<E : ErrorKind>(val errorKind: E) : ViewState<Nothing, E>()

        override fun toString(): String {
            return when (this) {
                is ShowContent -> content.toString()
                is ShowError -> errorKind.toString()
            }
        }
    }
}
