package soot.jimple.interproc.ifds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.interproc.ifds.flowfunc.FlowFunctions;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.flowfunc.SimpleFlowFunction;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				Collection<Local> universe = new HashSet<Local>();
				
				for(SootMethod m: Scene.v().getMainClass().getMethods()) {
					if(m.hasActiveBody())
						universe.addAll(m.getActiveBody().getLocals());					
				}
				
				for(SootMethod m: Scene.v().getMainClass().getMethods()) {
					if(m.hasActiveBody())
						System.err.println(m.getActiveBody());
				}

				
				Map<SootMethod, Set<Local>> initialSeeds = new HashMap<SootMethod, Set<Local>>();
				initialSeeds.put(Scene.v().getMainMethod(), Collections.singleton(Scene.v().getMainMethod().getActiveBody().getLocals().getFirst()));
				
				TabulationSolver<Unit,Local,SootMethod> solver = new TabulationSolver<Unit,Local,SootMethod>(
					new DefaultInterproceduralCFG(),
					new FlowFunctions<Unit,Local,SootMethod>() {

						public SimpleFlowFunction<Local> getNormalFlowFunction(Unit src, Unit dest) {
							if(src instanceof AssignStmt) {
								AssignStmt assignStmt = (AssignStmt) src;
								Value right = assignStmt.getRightOp();
								if(right instanceof Local) {
									final Local rightLocal = (Local) right;
									final Local leftLocal = (Local) assignStmt.getLeftOp();
									return new SimpleFlowFunction<Local>() {
										
										public Set<Local> computeTargets(@Nullable Local source) {
											if(source==null) return Collections.singleton(null);
											if(source.equals(rightLocal)) {
												Set<Local> res = new HashSet<Local>();
												res.add(source);
												res.add(leftLocal);
												return res;
											}
											return Collections.singleton(source);
										}
										
										public Set<Local> computeSources(@Nullable Local target) {
											if(target==null) return Collections.singleton(null);
											if(target.equals(rightLocal) || target.equals(leftLocal)) {
												return Collections.singleton(rightLocal);
											} 
											return Collections.singleton(target);
										}
									};
								}
							}
							return Identity.v();
						}

						public SimpleFlowFunction<Local> getCallFlowFunction(Unit src, SootMethod dest) {
							Stmt stmt = (Stmt) src;
							InvokeExpr ie = stmt.getInvokeExpr();
							final List<Value> callArgs = ie.getArgs();
							final List<Local> paramLocals = new ArrayList<Local>();
							for(int i=0;i<dest.getParameterCount();i++) {
								paramLocals.add(dest.getActiveBody().getParameterLocal(i));
							}
							return new SimpleFlowFunction<Local>() {

								public Set<Local> computeTargets(Local source) {
									if(source==null) return Collections.singleton(null);
									int argIndex = callArgs.indexOf(source);
									if(argIndex>-1) {
										Set<Local> res = new HashSet<Local>();
										res.add(source);
										res.add(paramLocals.get(argIndex));
										return res;
									}
									return Collections.singleton(source);
								}

								public Set<Local> computeSources(Local target) {
									if(target==null) return Collections.singleton(null);
									int paramIndex = paramLocals.indexOf(target);
									if(paramIndex>-1) {
										Value val = callArgs.get(paramIndex);
										if(val instanceof Local)
											return Collections.singleton((Local)val);
										else
											return Collections.emptySet();
									}
									return Collections.singleton(target);
								}
								
							};
						}

						public SimpleFlowFunction<Local> getReturnFlowFunction(SootMethod callee, Unit exitStmt, Unit retSite) {
							if (exitStmt instanceof ReturnStmt) {								
								ReturnStmt returnStmt = (ReturnStmt) exitStmt;
								Value op = returnStmt.getOp();
								if(op instanceof Local) {
									if(retSite instanceof DefinitionStmt) {
										DefinitionStmt defnStmt = (DefinitionStmt) retSite;
										Value leftOp = defnStmt.getLeftOp();
										if(leftOp instanceof Local) {
											final Local tgtLocal = (Local) leftOp;
											final Local retLocal = (Local) op;
											return new SimpleFlowFunction<Local>() {

												public Set<Local> computeTargets(Local source) {
													if(source==null) return Collections.singleton(null);
													if(source==retLocal)
														return Collections.singleton(tgtLocal);
													return Collections.emptySet();
												}

												public Set<Local> computeSources(Local target) {
													if(target==null) return Collections.singleton(null);
													if(target==tgtLocal) return Collections.singleton(retLocal);
													return Collections.emptySet();
												}
												
											};
										}
									}
								}
							} 
							return KillAll.v();
						}

						public SimpleFlowFunction<Local> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
							return Identity.v();
						}
					}, initialSeeds
				);	
				solver.solve();
			}
		}));
		
		soot.Main.main(args);
	}

}