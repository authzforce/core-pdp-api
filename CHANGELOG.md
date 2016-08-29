# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. This project adheres to [Semantic Versioning](http://semver.org).


## 7.0.0
### Added
- Dependency: com.koloboke:koloboke-impl-jdk6-7:1.0.0 for better (performance and API) HashMap/HashSet. More info:
http://java-performance.info/hashmap-overview-jdk-fastutil-goldman-sachs-hppc-koloboke-trove-january-2015/

### Changed
- CombiningAlg.Evaluator (Combining Algorithm evaluator interface): 
  - Return type changed to ExtendedDecision (Decision, Status, Extended Indeterminate if Decision is Indeterminate), simpler than formerly DecisionResult
  - evaluate() takes 2 extra "out" parameters: UpdatablePepActions and UpdatableApplicablePolicies used to add/return PEP actions and applicable policies collected during evaluation
- DecisionCache interface: input PdpDecisionInput and output PdpDecisionResult allow to handle 2 new fields: named attributes and extra Content nodes used during evaluation; thus enabling smarter caching possibilities
- EvaluationContext interface: addApplicablePolicy(...) replaces by isApplicablePolicyIdListRequested() because applicable policies are now collected in the new "out" parameter above and in the evaluation results (DecisionResult) returned by Policy evaluators
- Deprecated Expression#getJAXBElement() usually used to get the original XACML from which the Expression was parsed (no longer considered useful)
- Bag#equals() re-implemented like XACML function set-equals
- Switch implementation of unmodifidable lists to Guava ImmutableList
- Made all implementations of DecisionResult immutable


## 6.0.0
### Changed 
- Project parent version (3.4.0): all JAXB-annotated classes derived from XACML schema now implements java.io.Serializable interface. This affects subclasses StatusHelper, CombinerParameterEvaluator and concrete XXXValue classes (extending XACML AttributeValue)
- All method parameters made final when applicable
- IndividualDecisionRequest#isApplicablePolicyIdentifiersReturned() method renamed to isApplicablePolicyIdListReturned()

### Removed
- CombiningAlgSet and FunctionSet classes (GitHub issue #1), now useless.


## 5.0.0
### Changed
- Attribute Provider Extension interface (CloseableAttributeProviderModule interface): new parameter to pass global PDP environment properties to AttributeProvider extensions

## 4.0.2
### Fixed
- Code-style issues reported by Codacy

## 4.0.1
### Fixed
- Issues reported by Codacy


## 4.0.0
### Changed
- FirstOrderBagFunctions#getFunctions(): changed parameters to only one of type DatatypeFactory<AV> for simplification

### Fixed
- Current year in license header


## 3.8.0
### Added
- Implementations of XACML 3.0 Core standard data types
- Re-usable/abstract classes for XACML comparison/conversion/higher-order/set/bag functions

### Fixed
- Javadoc of DecisionResult#getExtendedIndeterminate() method


## 3.7.0
### Changed
- PDP extensions that are static root policy providers should now implement StaticRootPolicyProviderModule class, instead of RootPolicyProviderModule.Static class
- PDP extensions that are static ref-policy providers should now implement StaticRefPolicyProvider class, instead of RefPolicyProvider class with isStatic() method returning true
- (Static)RootPolicyProviderModule and (Static)RefPolicyProviderModule#get(...) return type is now (Static)TopLevelPolicyElementEvaluator instead of IPolicyEvaluator interface (removed)

### Added
- Interface method PolicyEvaluator#getExtraPolicyMetadata(): provides version of the evaluated Policy(Set) and policies referenced (directly/indirectly) from this Policy(Set)
- Interface method PolicyEvaluator#getPolicyElementType(): provides the type of top-level policy element (Policy or PolicySet).
- Interface method DecisionResult#getExtendedIndeterminate(): provides Extended Indeterminate value (to be used when #getDecision() returns "Indeterminate")


## 3.6.1
### Added
- Initial release on Github



