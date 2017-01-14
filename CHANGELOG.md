# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. This project adheres to [Semantic Versioning](http://semver.org).


## Unreleased
### Changed
- Parent project version: 4.0.0 -> 4.1.0 => Saxon-HE dependency version 9.7.0-11 -> 9.7.0-14


## 8.0.0
### Added
- Extension mechanism to switch HashMap/HashSet implementation; default implementation is based on native JRE and Guava.
- AtomicValue interface for atomic/primitive values, implemented by Function and AttributeValue
- Public class PrimitiveDatatype for primitive value datatypes
- ConstantExpression interface (replaces ValueExpression) for all constant Value expression
- FunctionExpression interface, Expression wrapper for Functions (Function no longer extends Expression but AtomicValue) like Value
- Function datatype constant in StandardDatatypes class, used as formal parameter type for functions in higher-order functions
- Maven plugin owasp-dependency-check to check vulnerabilities in dependencies 

### Changed
- Function no longer extends Expression but AtomicValue since Function Expression is now materialized by new FunctionExpression interface
- Expression interface: method boolean isStatic() replaced by getValue() to get the constant result if expression is static/constant (instead of calling evaluate(null) which forces callers the complexity of handling IndeterminateEvaluationException), null if not
- ExpressionFactory interface: Function return types replaced with FunctionExpression (new interface)
- FirstOrderFunctionCall abstract class (base class for first-order function call implementations): changed to interface and abstract class logic moved to new BaseFirstOrderFunctionCall class,
- DatatypeFactory interface: removed method isExpressionStatic(), now useless since we have new Expression#getValue() method 
- CombiningAlg (combining algorithm interface) Evaluator interface: more generic
- Maven parent project version: 3.4.0 -> 4.0.0:
	- **Java version: 1.7 -> 1.8** (maven.compiler.source/target property)
	- Guava dependency version: 18.0 -> 20.0
	- Saxon-HE dependency version: 9.6.0-5 -> 9.7.0-11
	- com.sun.mail:javax.mail v1.5.4 changed to com.sun.mail:mailapi v1.5.6

### Removed
- ValueExpression interface, replaced by ConstantExpression
- Dependency on Koloboke, replaced by extension mechanism mentioned in *Added* section that would allow to switch from the default HashMap/HashSet implementation to Koloboke-based.


## 7.1.1
### Fixed
- Javadoc issues


## 7.1.0
### Fixed
- Bag.equals() ignoring duplicates (like XACML set-equals function). Fixed by using Guava Multiset as backend structure and Multiset.equals(), to comply with the mathematical definition of a bag/multiset and XACML definition which is basically the same.
- BaseStaticRootPolicyProviderModule keeping a reference to static refPolicyProvider, although policies are to be resolved statically at initialization time, after that, it is no longer needed. Fix: remove BaseStaticRootPolicyProviderModule to force RootPoliyPovider modules to manage their refPolicyProvider and free memory after use.

### Added 
- Bag.elements() method, returns a Multiset (Guava) view of a bag's elements, useful in particular to implement functions with bags like XACML set-*

### Removed
- BaseStaticRootPolicyProviderModule class removed (see fix above)


## 7.0.0
### Added
- Dependency: com.koloboke:koloboke-impl-jdk6-7:1.0.0 for better (performance and API) HashMap/HashSet. More info:
http://java-performance.info/hashmap-overview-jdk-fastutil-goldman-sachs-hppc-koloboke-trove-january-2015/

### Changed
- CombiningAlg.Evaluator (Combining Algorithm evaluator interface): 
  - Return type changed to ExtendedDecision (Decision, Status, Extended Indeterminate if Decision is Indeterminate), simpler than formerly DecisionResult
  - evaluate() takes 2 extra "out" parameters: UpdatablePepActions and UpdatableApplicablePolicies used to add/return PEP actions and applicable policies collected during evaluation
- DecisionCache interface: input PdpDecisionInput and output PdpDecisionResult allow to handle 2 new fields: named attributes and extra Content nodes used during evaluation; thus enabling smarter caching possibilities
- EvaluationContext interface: addApplicablePolicy(...) replaced by isApplicablePolicyIdListRequested() because applicable policies are now collected in the new "out" parameter above and in the evaluation results (DecisionResult) returned by Policy evaluators
- Deprecated Expression#getJAXBElement() usually used to get the original XACML from which the Expression was parsed (no longer considered useful)
- Bag#equals() re-implemented like XACML function set-equals
- Change implementation of unmodifidable lists to Guava ImmutableList
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



