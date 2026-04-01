android-player

# vega-fx-app          eeee



Build Errors



Settings file 'D:\\Projects\\vega-fx-app\\settings.gradle' line: 6



A problem occurred evaluating settings 'vega-fx-app'.

> Could not find method jcenter() for arguments \[] on repository container of type org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler.



\* Try:

> Run with --info or --debug option to get more log output.

> Run with --scan to get full insights.

> Get more help at https://help.gradle.org.



\* Exception is:

org.gradle.api.GradleScriptException: A problem occurred evaluating settings 'vega-fx-app'.

&nbsp;	at org.gradle.groovy.scripts.internal.DefaultScriptRunnerFactory$ScriptRunnerImpl.run(DefaultScriptRunnerFactory.java:93)

&nbsp;	at org.gradle.configuration.DefaultScriptPluginFactory$ScriptPluginImpl.apply(DefaultScriptPluginFactory.java:118)

&nbsp;	at org.gradle.configuration.BuildOperationScriptPlugin$1.run(BuildOperationScriptPlugin.java:68)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:29)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:26)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:47)

&nbsp;	at org.gradle.configuration.BuildOperationScriptPlugin.lambda$apply$0(BuildOperationScriptPlugin.java:65)

&nbsp;	at org.gradle.internal.code.DefaultUserCodeApplicationContext.apply(DefaultUserCodeApplicationContext.java:44)

&nbsp;	at org.gradle.configuration.BuildOperationScriptPlugin.apply(BuildOperationScriptPlugin.java:65)

&nbsp;	at org.gradle.initialization.ScriptEvaluatingSettingsProcessor.applySettingsScript(ScriptEvaluatingSettingsProcessor.java:75)

&nbsp;	at org.gradle.initialization.ScriptEvaluatingSettingsProcessor.process(ScriptEvaluatingSettingsProcessor.java:68)

&nbsp;	at org.gradle.initialization.SettingsEvaluatedCallbackFiringSettingsProcessor.process(SettingsEvaluatedCallbackFiringSettingsProcessor.java:34)

&nbsp;	at org.gradle.initialization.RootBuildCacheControllerSettingsProcessor.process(RootBuildCacheControllerSettingsProcessor.java:47)

&nbsp;	at org.gradle.initialization.BuildOperationSettingsProcessor$2.call(BuildOperationSettingsProcessor.java:49)

&nbsp;	at org.gradle.initialization.BuildOperationSettingsProcessor$2.call(BuildOperationSettingsProcessor.java:46)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)

&nbsp;	at org.gradle.initialization.BuildOperationSettingsProcessor.process(BuildOperationSettingsProcessor.java:46)

&nbsp;	at org.gradle.initialization.DefaultSettingsLoader.findSettingsAndLoadIfAppropriate(DefaultSettingsLoader.java:183)

&nbsp;	at org.gradle.initialization.DefaultSettingsLoader.findAndLoadSettings(DefaultSettingsLoader.java:86)

&nbsp;	at org.gradle.initialization.SettingsAttachingSettingsLoader.findAndLoadSettings(SettingsAttachingSettingsLoader.java:33)

&nbsp;	at org.gradle.internal.composite.CommandLineIncludedBuildSettingsLoader.findAndLoadSettings(CommandLineIncludedBuildSettingsLoader.java:35)

&nbsp;	at org.gradle.internal.composite.ChildBuildRegisteringSettingsLoader.findAndLoadSettings(ChildBuildRegisteringSettingsLoader.java:44)

&nbsp;	at org.gradle.internal.composite.CompositeBuildSettingsLoader.findAndLoadSettings(CompositeBuildSettingsLoader.java:35)

&nbsp;	at org.gradle.initialization.InitScriptHandlingSettingsLoader.findAndLoadSettings(InitScriptHandlingSettingsLoader.java:33)

&nbsp;	at org.gradle.api.internal.initialization.CacheConfigurationsHandlingSettingsLoader.findAndLoadSettings(CacheConfigurationsHandlingSettingsLoader.java:36)

&nbsp;	at org.gradle.initialization.GradlePropertiesHandlingSettingsLoader.findAndLoadSettings(GradlePropertiesHandlingSettingsLoader.java:38)

&nbsp;	at org.gradle.initialization.DefaultSettingsPreparer.prepareSettings(DefaultSettingsPreparer.java:31)

&nbsp;	at org.gradle.initialization.BuildOperationFiringSettingsPreparer$LoadBuild.doLoadBuild(BuildOperationFiringSettingsPreparer.java:70)

&nbsp;	at org.gradle.initialization.BuildOperationFiringSettingsPreparer$LoadBuild.run(BuildOperationFiringSettingsPreparer.java:65)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:29)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$1.execute(DefaultBuildOperationRunner.java:26)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.run(DefaultBuildOperationRunner.java:47)

&nbsp;	at org.gradle.initialization.BuildOperationFiringSettingsPreparer.prepareSettings(BuildOperationFiringSettingsPreparer.java:53)

&nbsp;	at org.gradle.initialization.VintageBuildModelController.lambda$prepareSettings$1(VintageBuildModelController.java:79)

&nbsp;	at org.gradle.internal.model.StateTransitionController.lambda$doTransition$14(StateTransitionController.java:255)

&nbsp;	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:266)

&nbsp;	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:254)

&nbsp;	at org.gradle.internal.model.StateTransitionController.lambda$transitionIfNotPreviously$11(StateTransitionController.java:213)

&nbsp;	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:35)

&nbsp;	at org.gradle.internal.model.StateTransitionController.transitionIfNotPreviously(StateTransitionController.java:209)

&nbsp;	at org.gradle.initialization.VintageBuildModelController.prepareSettings(VintageBuildModelController.java:79)

&nbsp;	at org.gradle.initialization.VintageBuildModelController.getLoadedSettings(VintageBuildModelController.java:56)

&nbsp;	at org.gradle.internal.model.StateTransitionController.lambda$notInState$3(StateTransitionController.java:132)

&nbsp;	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:45)

&nbsp;	at org.gradle.internal.model.StateTransitionController.notInState(StateTransitionController.java:128)

&nbsp;	at org.gradle.internal.build.DefaultBuildLifecycleController.loadSettings(DefaultBuildLifecycleController.java:118)

&nbsp;	at org.gradle.internal.build.AbstractBuildState.ensureProjectsLoaded(AbstractBuildState.java:129)

&nbsp;	at org.gradle.plugins.ide.internal.tooling.GradleBuildBuilder.create(GradleBuildBuilder.java:50)

&nbsp;	at org.gradle.plugins.ide.internal.tooling.GradleBuildBuilder.create(GradleBuildBuilder.java:36)

&nbsp;	at org.gradle.tooling.provider.model.internal.DefaultToolingModelBuilderRegistry$BuildScopedBuilder.build(DefaultToolingModelBuilderRegistry.java:210)

&nbsp;	at org.gradle.tooling.provider.model.internal.DefaultToolingModelBuilderRegistry$BuildOperationWrappingBuilder$1.call(DefaultToolingModelBuilderRegistry.java:341)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)

&nbsp;	at org.gradle.tooling.provider.model.internal.DefaultToolingModelBuilderRegistry$BuildOperationWrappingBuilder.build(DefaultToolingModelBuilderRegistry.java:338)

&nbsp;	at org.gradle.internal.build.DefaultBuildToolingModelController$AbstractToolingScope.getModel(DefaultBuildToolingModelController.java:83)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeModelCreator$DefaultBuildTreeModelController.getModelForScope(DefaultBuildTreeModelCreator.java:146)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeModelCreator$DefaultBuildTreeModelController.access$300(DefaultBuildTreeModelCreator.java:70)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeModelCreator$DefaultBuildTreeModelController$1.call(DefaultBuildTreeModelCreator.java:86)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeModelCreator$DefaultBuildTreeModelController.getModel(DefaultBuildTreeModelCreator.java:81)

&nbsp;	at org.gradle.tooling.internal.provider.runner.DefaultBuildController.getModel(DefaultBuildController.java:104)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.ParameterAwareBuildControllerAdapter.getModel(ParameterAwareBuildControllerAdapter.java:40)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.UnparameterizedBuildController.getModel(UnparameterizedBuildController.java:116)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.NestedActionAwareBuildControllerAdapter.getModel(NestedActionAwareBuildControllerAdapter.java:32)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.UnparameterizedBuildController.getModel(UnparameterizedBuildController.java:79)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.NestedActionAwareBuildControllerAdapter.getModel(NestedActionAwareBuildControllerAdapter.java:32)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.UnparameterizedBuildController.getBuildModel(UnparameterizedBuildController.java:74)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.NestedActionAwareBuildControllerAdapter.getBuildModel(NestedActionAwareBuildControllerAdapter.java:32)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.lambda$initAction$7(GradleModelFetchAction.java:165)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.telemetry.GradleOpenTelemetry.callWithSpan(GradleOpenTelemetry.java:55)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.telemetry.GradleOpenTelemetry.callWithSpan(GradleOpenTelemetry.java:31)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.initAction(GradleModelFetchAction.java:164)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.lambda$doExecute$4(GradleModelFetchAction.java:118)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.telemetry.GradleOpenTelemetry.callWithSpan(GradleOpenTelemetry.java:55)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.telemetry.GradleOpenTelemetry.callWithSpan(GradleOpenTelemetry.java:31)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.doExecute(GradleModelFetchAction.java:117)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.lambda$execute$1(GradleModelFetchAction.java:103)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.telemetry.GradleOpenTelemetry.callWithSpan(GradleOpenTelemetry.java:55)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.telemetry.GradleOpenTelemetry.callWithSpan(GradleOpenTelemetry.java:31)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.lambda$execute$2(GradleModelFetchAction.java:102)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.withOpenTelemetry(GradleModelFetchAction.java:297)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.lambda$execute$3(GradleModelFetchAction.java:101)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.util.GradleExecutorServiceUtil.withSingleThreadExecutor(GradleExecutorServiceUtil.java:18)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.execute(GradleModelFetchAction.java:100)

&nbsp;	at com.intellij.gradle.toolingExtension.impl.modelAction.GradleModelFetchAction.execute(GradleModelFetchAction.java:33)

&nbsp;	at org.gradle.tooling.internal.consumer.connection.InternalBuildActionAdapter.execute(InternalBuildActionAdapter.java:65)

&nbsp;	at org.gradle.tooling.internal.provider.runner.AbstractClientProvidedBuildActionRunner$ActionAdapter.executeAction(AbstractClientProvidedBuildActionRunner.java:108)

&nbsp;	at org.gradle.tooling.internal.provider.runner.AbstractClientProvidedBuildActionRunner$ActionAdapter.runAction(AbstractClientProvidedBuildActionRunner.java:96)

&nbsp;	at org.gradle.tooling.internal.provider.runner.AbstractClientProvidedBuildActionRunner$ActionAdapter.beforeTasks(AbstractClientProvidedBuildActionRunner.java:80)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeModelCreator.beforeTasks(DefaultBuildTreeModelCreator.java:62)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.lambda$fromBuildModel$2(DefaultBuildTreeLifecycleController.java:83)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.lambda$runBuild$4(DefaultBuildTreeLifecycleController.java:120)

&nbsp;	at org.gradle.internal.model.StateTransitionController.lambda$transition$6(StateTransitionController.java:169)

&nbsp;	at org.gradle.internal.model.StateTransitionController.doTransition(StateTransitionController.java:266)

&nbsp;	at org.gradle.internal.model.StateTransitionController.lambda$transition$7(StateTransitionController.java:169)

&nbsp;	at org.gradle.internal.work.DefaultSynchronizer.withLock(DefaultSynchronizer.java:45)

&nbsp;	at org.gradle.internal.model.StateTransitionController.transition(StateTransitionController.java:169)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.runBuild(DefaultBuildTreeLifecycleController.java:117)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeLifecycleController.fromBuildModel(DefaultBuildTreeLifecycleController.java:82)

&nbsp;	at org.gradle.tooling.internal.provider.runner.AbstractClientProvidedBuildActionRunner.runClientAction(AbstractClientProvidedBuildActionRunner.java:42)

&nbsp;	at org.gradle.tooling.internal.provider.runner.ClientProvidedPhasedActionRunner.run(ClientProvidedPhasedActionRunner.java:58)

&nbsp;	at org.gradle.launcher.exec.ChainingBuildActionRunner.run(ChainingBuildActionRunner.java:35)

&nbsp;	at org.gradle.internal.buildtree.ProblemReportingBuildActionRunner.run(ProblemReportingBuildActionRunner.java:49)

&nbsp;	at org.gradle.launcher.exec.BuildOutcomeReportingBuildActionRunner.run(BuildOutcomeReportingBuildActionRunner.java:71)

&nbsp;	at org.gradle.tooling.internal.provider.FileSystemWatchingBuildActionRunner.run(FileSystemWatchingBuildActionRunner.java:135)

&nbsp;	at org.gradle.launcher.exec.BuildCompletionNotifyingBuildActionRunner.run(BuildCompletionNotifyingBuildActionRunner.java:41)

&nbsp;	at org.gradle.launcher.exec.RootBuildLifecycleBuildActionExecutor.lambda$execute$0(RootBuildLifecycleBuildActionExecutor.java:54)

&nbsp;	at org.gradle.composite.internal.DefaultRootBuildState.run(DefaultRootBuildState.java:130)

&nbsp;	at org.gradle.launcher.exec.RootBuildLifecycleBuildActionExecutor.execute(RootBuildLifecycleBuildActionExecutor.java:54)

&nbsp;	at org.gradle.internal.buildtree.InitDeprecationLoggingActionExecutor.execute(InitDeprecationLoggingActionExecutor.java:62)

&nbsp;	at org.gradle.internal.buildtree.InitProblems.execute(InitProblems.java:36)

&nbsp;	at org.gradle.internal.buildtree.DefaultBuildTreeContext.execute(DefaultBuildTreeContext.java:40)

&nbsp;	at org.gradle.launcher.exec.BuildTreeLifecycleBuildActionExecutor.lambda$execute$0(BuildTreeLifecycleBuildActionExecutor.java:71)

&nbsp;	at org.gradle.internal.buildtree.BuildTreeState.run(BuildTreeState.java:60)

&nbsp;	at org.gradle.launcher.exec.BuildTreeLifecycleBuildActionExecutor.execute(BuildTreeLifecycleBuildActionExecutor.java:71)

&nbsp;	at org.gradle.launcher.exec.RunAsBuildOperationBuildActionExecutor$2.call(RunAsBuildOperationBuildActionExecutor.java:67)

&nbsp;	at org.gradle.launcher.exec.RunAsBuildOperationBuildActionExecutor$2.call(RunAsBuildOperationBuildActionExecutor.java:63)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:209)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$CallableBuildOperationWorker.execute(DefaultBuildOperationRunner.java:204)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:66)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner$2.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:166)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.execute(DefaultBuildOperationRunner.java:59)

&nbsp;	at org.gradle.internal.operations.DefaultBuildOperationRunner.call(DefaultBuildOperationRunner.java:53)

&nbsp;	at org.gradle.launcher.exec.RunAsBuildOperationBuildActionExecutor.execute(RunAsBuildOperationBuildActionExecutor.java:63)

&nbsp;	at org.gradle.launcher.exec.RunAsWorkerThreadBuildActionExecutor.lambda$execute$0(RunAsWorkerThreadBuildActionExecutor.java:36)

&nbsp;	at org.gradle.internal.work.DefaultWorkerLeaseService.withLocks(DefaultWorkerLeaseService.java:263)

&nbsp;	at org.gradle.internal.work.DefaultWorkerLeaseService.runAsWorkerThread(DefaultWorkerLeaseService.java:127)

&nbsp;	at org.gradle.launcher.exec.RunAsWorkerThreadBuildActionExecutor.execute(RunAsWorkerThreadBuildActionExecutor.java:36)

&nbsp;	at org.gradle.tooling.internal.provider.continuous.ContinuousBuildActionExecutor.execute(ContinuousBuildActionExecutor.java:110)

&nbsp;	at org.gradle.tooling.internal.provider.SubscribableBuildActionExecutor.execute(SubscribableBuildActionExecutor.java:64)

&nbsp;	at org.gradle.internal.session.DefaultBuildSessionContext.execute(DefaultBuildSessionContext.java:46)

&nbsp;	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor$ActionImpl.apply(BuildSessionLifecycleBuildActionExecutor.java:92)

&nbsp;	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor$ActionImpl.apply(BuildSessionLifecycleBuildActionExecutor.java:80)

&nbsp;	at org.gradle.internal.session.BuildSessionState.run(BuildSessionState.java:73)

&nbsp;	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor.execute(BuildSessionLifecycleBuildActionExecutor.java:62)

&nbsp;	at org.gradle.internal.buildprocess.execution.BuildSessionLifecycleBuildActionExecutor.execute(BuildSessionLifecycleBuildActionExecutor.java:41)

&nbsp;	at org.gradle.internal.buildprocess.execution.StartParamsValidatingActionExecutor.execute(StartParamsValidatingActionExecutor.java:64)

&nbsp;	at org.gradle.internal.buildprocess.execution.StartParamsValidatingActionExecutor.execute(StartParamsValidatingActionExecutor.java:32)

&nbsp;	at org.gradle.internal.buildprocess.execution.SessionFailureReportingActionExecutor.execute(SessionFailureReportingActionExecutor.java:51)

&nbsp;	at org.gradle.internal.buildprocess.execution.SessionFailureReportingActionExecutor.execute(SessionFailureReportingActionExecutor.java:39)

&nbsp;	at org.gradle.internal.buildprocess.execution.SetupLoggingActionExecutor.execute(SetupLoggingActionExecutor.java:47)

&nbsp;	at org.gradle.internal.buildprocess.execution.SetupLoggingActionExecutor.execute(SetupLoggingActionExecutor.java:31)

&nbsp;	at org.gradle.launcher.daemon.server.exec.ExecuteBuild.doBuild(ExecuteBuild.java:70)

&nbsp;	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.WatchForDisconnection.execute(WatchForDisconnection.java:39)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.ResetDeprecationLogger.execute(ResetDeprecationLogger.java:29)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.RequestStopIfSingleUsedDaemon.execute(RequestStopIfSingleUsedDaemon.java:35)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.ForwardClientInput.lambda$execute$0(ForwardClientInput.java:40)

&nbsp;	at org.gradle.internal.daemon.clientinput.ClientInputForwarder.forwardInput(ClientInputForwarder.java:80)

&nbsp;	at org.gradle.launcher.daemon.server.exec.ForwardClientInput.execute(ForwardClientInput.java:37)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.LogAndCheckHealth.execute(LogAndCheckHealth.java:64)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.LogToClient.doBuild(LogToClient.java:63)

&nbsp;	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.EstablishBuildEnvironment.doBuild(EstablishBuildEnvironment.java:84)

&nbsp;	at org.gradle.launcher.daemon.server.exec.BuildCommandOnly.execute(BuildCommandOnly.java:37)

&nbsp;	at org.gradle.launcher.daemon.server.api.DaemonCommandExecution.proceed(DaemonCommandExecution.java:104)

&nbsp;	at org.gradle.launcher.daemon.server.exec.StartBuildOrRespondWithBusy$1.run(StartBuildOrRespondWithBusy.java:52)

&nbsp;	at org.gradle.launcher.daemon.server.DaemonStateCoordinator.lambda$runCommand$0(DaemonStateCoordinator.java:321)

&nbsp;	at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:64)

&nbsp;	at org.gradle.internal.concurrent.AbstractManagedExecutor$1.run(AbstractManagedExecutor.java:47)

Caused by: org.gradle.internal.metaobject.AbstractDynamicObject$CustomMessageMissingMethodException: Could not find method jcenter() for arguments \[] on repository container of type org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler.

&nbsp;	at org.gradle.internal.metaobject.AbstractDynamicObject$CustomMissingMethodExecutionFailed.<init>(AbstractDynamicObject.java:195)

&nbsp;	at org.gradle.internal.metaobject.AbstractDynamicObject.methodMissingException(AbstractDynamicObject.java:189)

&nbsp;	at org.gradle.internal.metaobject.ConfigureDelegate.invokeMethod(ConfigureDelegate.java:84)

&nbsp;	at settings\_1tsfm1anmozappu05066uiqxu$\_run\_closure1$\_closure2.doCall$original(D:\\Projects\\vega-fx-app\\settings.gradle:6)

&nbsp;	at settings\_1tsfm1anmozappu05066uiqxu$\_run\_closure1$\_closure2.doCall(D:\\Projects\\vega-fx-app\\settings.gradle)

&nbsp;	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)

&nbsp;	at org.gradle.util.internal.ClosureBackedAction.execute(ClosureBackedAction.java:73)

&nbsp;	at org.gradle.util.internal.ConfigureUtil.configureTarget(ConfigureUtil.java:166)

&nbsp;	at org.gradle.util.internal.ConfigureUtil.configureSelf(ConfigureUtil.java:142)

&nbsp;	at org.gradle.api.internal.artifacts.DefaultArtifactRepositoryContainer.configure(DefaultArtifactRepositoryContainer.java:65)

&nbsp;	at org.gradle.api.internal.artifacts.DefaultArtifactRepositoryContainer.configure(DefaultArtifactRepositoryContainer.java:35)

&nbsp;	at org.gradle.util.internal.ConfigureUtil.configure(ConfigureUtil.java:105)

&nbsp;	at org.gradle.util.internal.ConfigureUtil$WrappedConfigureAction.execute(ConfigureUtil.java:178)

&nbsp;	at org.gradle.plugin.management.internal.DefaultPluginManagementSpec.repositories(DefaultPluginManagementSpec.java:57)

&nbsp;	at org.gradle.plugin.management.internal.DefaultPluginManagementSpec\_Decorated.repositories(Unknown Source)

&nbsp;	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)

&nbsp;	at org.gradle.internal.metaobject.BeanDynamicObject$MetaClassAdapter.invokeMethod(BeanDynamicObject.java:571)

&nbsp;	at org.gradle.internal.metaobject.BeanDynamicObject.tryInvokeMethod(BeanDynamicObject.java:223)

&nbsp;	at org.gradle.internal.metaobject.CompositeDynamicObject.tryInvokeMethod(CompositeDynamicObject.java:111)

&nbsp;	at org.gradle.internal.extensibility.MixInClosurePropertiesAsMethodsDynamicObject.tryInvokeMethod(MixInClosurePropertiesAsMethodsDynamicObject.java:37)

&nbsp;	at org.gradle.internal.metaobject.ConfigureDelegate.invokeMethod(ConfigureDelegate.java:65)

&nbsp;	at settings\_1tsfm1anmozappu05066uiqxu$\_run\_closure1.doCall$original(D:\\Projects\\vega-fx-app\\settings.gradle:2)

&nbsp;	at settings\_1tsfm1anmozappu05066uiqxu$\_run\_closure1.doCall(D:\\Projects\\vega-fx-app\\settings.gradle)

&nbsp;	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)

&nbsp;	at org.gradle.util.internal.ClosureBackedAction.execute(ClosureBackedAction.java:73)

&nbsp;	at org.gradle.util.internal.ConfigureUtil.configureTarget(ConfigureUtil.java:166)

&nbsp;	at org.gradle.util.internal.ConfigureUtil.configure(ConfigureUtil.java:107)

&nbsp;	at org.gradle.util.internal.ConfigureUtil$WrappedConfigureAction.execute(ConfigureUtil.java:178)

&nbsp;	at org.gradle.initialization.DefaultSettings.pluginManagement(DefaultSettings.java:349)

&nbsp;	at org.gradle.initialization.DefaultSettings\_Decorated.pluginManagement(Unknown Source)

&nbsp;	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(Unknown Source)

&nbsp;	at org.gradle.internal.metaobject.BeanDynamicObject$MetaClassAdapter.invokeMethod(BeanDynamicObject.java:571)

&nbsp;	at org.gradle.internal.metaobject.BeanDynamicObject.tryInvokeMethod(BeanDynamicObject.java:223)

&nbsp;	at org.gradle.internal.metaobject.CompositeDynamicObject.tryInvokeMethod(CompositeDynamicObject.java:111)

&nbsp;	at org.gradle.internal.extensibility.MixInClosurePropertiesAsMethodsDynamicObject.tryInvokeMethod(MixInClosurePropertiesAsMethodsDynamicObject.java:37)

&nbsp;	at org.gradle.groovy.scripts.BasicScript$ScriptDynamicObject.tryInvokeMethod(BasicScript.java:138)

&nbsp;	at org.gradle.internal.metaobject.AbstractDynamicObject.invokeMethod(AbstractDynamicObject.java:168)

&nbsp;	at org.gradle.api.internal.project.DefaultDynamicLookupRoutine.invokeMethod(DefaultDynamicLookupRoutine.java:58)

&nbsp;	at org.gradle.groovy.scripts.BasicScript.invokeMethod(BasicScript.java:87)

&nbsp;	at settings\_1tsfm1anmozappu05066uiqxu.run(D:\\Projects\\vega-fx-app\\settings.gradle:1)

&nbsp;	at org.gradle.groovy.scripts.internal.DefaultScriptRunnerFactory$ScriptRunnerImpl.run(DefaultScriptRunnerFactory.java:91)

&nbsp;	... 192 more

Fix

