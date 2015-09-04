function [methodinfo,structs,enuminfo,ThunkLibName]=bcvtb
%BCVTB Create structures to define interfaces found in 'matlabSocket'.

%This function was generated by loadlibrary.m parser version  on Fri Sep  4 15:40:49 2015
%perl options:'matlabSocket.i -outfile=bcvtb.m -thunkfile=libbcvtb_thunk_glnxa64.c -header=matlabSocket.h'
ival={cell(1,0)}; % change 0 to the actual number of functions to preallocate the data.
structs=[];enuminfo=[];fcnNum=1;
fcns=struct('name',ival,'calltype',ival,'LHS',ival,'RHS',ival,'alias',ival,'thunkname', ival);
MfilePath=fileparts(mfilename('fullpath'));
ThunkLibName=fullfile(MfilePath,'libbcvtb_thunk_glnxa64');
% int establishclientsocket ( const char * const docname ); 
fcns.thunkname{fcnNum}='int32cstringThunk';fcns.name{fcnNum}='establishclientsocket'; fcns.calltype{fcnNum}='Thunk'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'cstring'};fcnNum=fcnNum+1;
% int sendclienterror ( const int * sockfd , const int * flaWri ); 
fcns.thunkname{fcnNum}='int32voidPtrvoidPtrThunk';fcns.name{fcnNum}='sendclienterror'; fcns.calltype{fcnNum}='Thunk'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'int32Ptr', 'int32Ptr'};fcnNum=fcnNum+1;
% int exchangedoubleswithsocket ( const int * sockfd , const int * flaWri , int * flaRea , const int * nDblWri , int * nDblRea , double * simTimWri , double dblValWri [], double * simTimRea , double dblValRea []); 
fcns.thunkname{fcnNum}='int32voidPtrvoidPtrvoidPtrvoidPtrvoidPtrvoidPtrvoidPtrvoidPtrvoidPtrThunk';fcns.name{fcnNum}='exchangedoubleswithsocket'; fcns.calltype{fcnNum}='Thunk'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'int32Ptr', 'doublePtr', 'doublePtr', 'doublePtr', 'doublePtr'};fcnNum=fcnNum+1;
% int closeipc ( int * sockfd ); 
fcns.thunkname{fcnNum}='int32voidPtrThunk';fcns.name{fcnNum}='closeipc'; fcns.calltype{fcnNum}='Thunk'; fcns.LHS{fcnNum}='int32'; fcns.RHS{fcnNum}={'int32Ptr'};fcnNum=fcnNum+1;
methodinfo=fcns;