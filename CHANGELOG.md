# Change log
All notable changes to this project are documented in this file following the [Keep a CHANGELOG](http://keepachangelog.com) conventions. This project adheres to [Semantic Versioning](http://semver.org).

## Unreleased
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



