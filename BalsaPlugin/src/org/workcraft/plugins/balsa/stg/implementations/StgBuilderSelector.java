package org.workcraft.plugins.balsa.stg.implementations;

public final class StgBuilderSelector {

    public static org.workcraft.plugins.balsa.stg.ComponentStgBuilder<org.workcraft.plugins.balsa.components.DynamicComponent> create(java.lang.String componentName) {
        if (componentName.equals("UnaryFuncPush"))
            return new UnaryFuncPushStgBuilder();
         else
            if (componentName.equals("SynchPull"))
                return new SynchPullStgBuilder();
             else
                if (componentName.equals("PassivatorPush"))
                    return new PassivatorPushStgBuilder();
                 else
                    if (componentName.equals("BinaryFuncConstRPush"))
                        return new BinaryFuncConstRPushStgBuilder();
                     else
                        if (componentName.equals("PassiveSyncEagerFalseVariable"))
                            return new PassiveSyncEagerFalseVariableStgBuilder();
                         else
                            if (componentName.equals("Fetch"))
                                return new FetchStgBuilder();
                             else
                                if (componentName.equals("Halt"))
                                    return new HaltStgBuilder();
                                 else
                                    if (componentName.equals("Bar"))
                                        return new BarStgBuilder();
                                     else
                                        if (componentName.equals("BinaryFunc"))
                                            return new BinaryFuncStgBuilder();
                                         else
                                            if (componentName.equals("BuiltinVariable"))
                                                return new BuiltinVariableStgBuilder();
                                             else
                                                if (componentName.equals("Call"))
                                                    return new CallStgBuilder();
                                                 else
                                                    if (componentName.equals("ActiveEagerFalseVariable"))
                                                        return new ActiveEagerFalseVariableStgBuilder();
                                                     else
                                                        if (componentName.equals("Concur"))
                                                            return new ConcurStgBuilder();
                                                         else
                                                            if (componentName.equals("Fork"))
                                                                return new ForkStgBuilder();
                                                             else
                                                                if (componentName.equals("ActiveEagerNullAdapt"))
                                                                    return new ActiveEagerNullAdaptStgBuilder();
                                                                 else
                                                                    if (componentName.equals("CaseFetch"))
                                                                        return new CaseFetchStgBuilder();
                                                                     else
                                                                        if (componentName.equals("WireFork"))
                                                                            return new WireForkStgBuilder();
                                                                         else
                                                                            if (componentName.equals("CallActive"))
                                                                                return new CallActiveStgBuilder();
                                                                             else
                                                                                if (componentName.equals("BinaryFuncConstR"))
                                                                                    return new BinaryFuncConstRStgBuilder();
                                                                                 else
                                                                                    if (componentName.equals("CombineEqual"))
                                                                                        return new CombineEqualStgBuilder();
                                                                                     else
                                                                                        if (componentName.equals("Slice"))
                                                                                            return new SliceStgBuilder();
                                                                                         else
                                                                                            if (componentName.equals("UnaryFunc"))
                                                                                                return new UnaryFuncStgBuilder();
                                                                                             else
                                                                                                    if (componentName.equals("CallDemux"))
                                                                                                        return new CallDemuxStgBuilder();
                                                                                                     else
                                                                                                        if (componentName.equals("ForkPush"))
                                                                                                            return new ForkPushStgBuilder();
                                                                                                         else
                                                                                                            if (componentName.equals("CallMux"))
                                                                                                                return new CallMuxStgBuilder();
                                                                                                             else
                                                                                                                if (componentName.equals("Passivator"))
                                                                                                                    return new PassivatorStgBuilder();
                                                                                                                 else
                                                                                                                    if (componentName.equals("Constant"))
                                                                                                                        return new ConstantStgBuilder();
                                                                                                                     else
                                                                                                                        if (componentName.equals("Adapt"))
                                                                                                                            return new AdaptStgBuilder();
                                                                                                                         else
                                                                                                                            if (componentName.equals("CallDemuxPush"))
                                                                                                                                return new CallDemuxPushStgBuilder();
                                                                                                                             else
                                                                                                                                if (componentName.equals("PassiveEagerNullAdapt"))
                                                                                                                                    return new PassiveEagerNullAdaptStgBuilder();
                                                                                                                                 else
                                                                                                                                    if (componentName.equals("Continue"))
                                                                                                                                        return new ContinueStgBuilder();
                                                                                                                                     else
                                                                                                                                        if (componentName.equals("While"))
                                                                                                                                            return new WhileStgBuilder();
                                                                                                                                         else
                                                                                                                                            if (componentName.equals("Sequence"))
                                                                                                                                                return new SequenceStgBuilder();
                                                                                                                                             else
                                                                                                                                                if (componentName.equals("Case"))
                                                                                                                                                    return new CaseStgBuilder();
                                                                                                                                                 else
                                                                                                                                                    if (componentName.equals("Encode"))
                                                                                                                                                        return new EncodeStgBuilder();
                                                                                                                                                     else
                                                                                                                                                        if (componentName.equals("PassiveEagerFalseVariable"))
                                                                                                                                                            return new PassiveEagerFalseVariableStgBuilder();
                                                                                                                                                         else
                                                                                                                                                            if (componentName.equals("SequenceOptimised"))
                                                                                                                                                                return new SequenceOptimisedStgBuilder();
                                                                                                                                                             else
                                                                                                                                                                if (componentName.equals("HaltPush"))
                                                                                                                                                                    return new HaltPushStgBuilder();
                                                                                                                                                                 else
                                                                                                                                                                    if (componentName.equals("FalseVariable"))
                                                                                                                                                                        return new FalseVariableStgBuilder();
                                                                                                                                                                     else
                                                                                                                                                                            if (componentName.equals("DecisionWait"))
                                                                                                                                                                                return new DecisionWaitStgBuilder();
                                                                                                                                                                             else
                                                                                                                                                                                if (componentName.equals("Variable"))
                                                                                                                                                                                    return new VariableStgBuilder();
                                                                                                                                                                                 else
                                                                                                                                                                                    if (componentName.equals("Arbiter"))
                                                                                                                                                                                        return new ArbiterStgBuilder();
                                                                                                                                                                                     else
                                                                                                                                                                                        if (componentName.equals("Split"))
                                                                                                                                                                                            return new SplitStgBuilder();
                                                                                                                                                                                         else
                                                                                                                                                                                            if (componentName.equals("NullAdapt"))
                                                                                                                                                                                                return new NullAdaptStgBuilder();
                                                                                                                                                                                             else
                                                                                                                                                                                                if (componentName.equals("BinaryFuncPush"))
                                                                                                                                                                                                    return new BinaryFuncPushStgBuilder();
                                                                                                                                                                                                 else
                                                                                                                                                                                                    if (componentName.equals("Synch"))
                                                                                                                                                                                                        return new SynchStgBuilder();
                                                                                                                                                                                                     else
                                                                                                                                                                                                        if (componentName.equals("Loop"))
                                                                                                                                                                                                            return new LoopStgBuilder();
                                                                                                                                                                                                         else
                                                                                                                                                                                                            if (componentName.equals("Combine"))
                                                                                                                                                                                                                return new CombineStgBuilder();
                                                                                                                                                                                                             else
                                                                                                                                                                                                                if (componentName.equals("ContinuePush"))
                                                                                                                                                                                                                    return new ContinuePushStgBuilder();
                                                                                                                                                                                                                 else
                                                                                                                                                                                                                    if (componentName.equals("SplitEqual"))
                                                                                                                                                                                                                        return new SplitEqualStgBuilder();
                                                                                                                                                                                                                     else
                                                                                                                                                                                                                        if (componentName.equals("SynchPush"))
                                                                                                                                                                                                                            return new SynchPushStgBuilder();
                                                                                                                                                                                                                         else
                                                                                                                                                                                                                            throw new org.workcraft.exceptions.NotSupportedException();





















































    }
}
