# flask packages
from flask_restful import Api

# project resources
from controllers import CustomerRegisterApi,CustomerLoginApi,GreetingApi,InterestApi,ProfessionApi,BusinessTypeApi, \
    ArticleApi,ArticlePublicApi,CreditTagApi,CreditApi,CreditCustomerApi,CreditBalanceCustomerApi,CreditBalanceApi, \
    SettingsApi,CustomerApi,ContactApi,ContactSummaryApi,ContactListShareApi,ContactListSaveApi,AccessTokenVerify,LogoutApi,StartupApi, \
    UserLoginApi,RolesApi,UserApi,UsersApi,MenuApi,MenusApi,MenuAllApi,CustomerProfileApi,ContactSaveApi,GetFile,GetArticle, FirebaseTokenApi, \
    AffiliateApi,NotificationApi,NotificationSendApi,AffiliateSummaryApi,WithdrawApi,WithdrawSummaryApi,RequestWithdrawApi,TotalAmountWithdrawApi, \
    CustomerAllApi,NotificationPaginateApi,ArticleAll,BusinessTypePaginateApi,ProfessionPaginateApi, \
    InterestPaginateApi,GreetingPaginateApi,SettingsPaginateApi,RolesAccessApi,RoleAccessApi,RoleAccessAllApi,RoleAllApi,RoleApi,UserPaginateApi, \
    UbahPasswordApi,BroadcastContactSaveApi,ContactDetailApi,ProvinceApi,ProvincePaginateApi,CityApi,CityPaginateApi,AffiliateKomisiApi,AffiliatePenghasilanApi, \
    UpdateFotoApi,GetProfileImage,UpdateCoverApi,RemoveFotoApi,RemoveCoverApi,UplineProfileApi,DownlineApi,AffiliatePenghasilanDetailApi,CityListApi, \
    ArticlePublicCustomerApi,DashboardApi,CreditDatatablesApi,SaveContactManualApi,FeedAdmin,GetFeed,FeedPublicCustomerApi,FeedAdminStatus,FeedCustomer,SlidersApi,SliderApi,SliderCustomerApi, \
    ClickAdsFeed,ListFeedPublicCustomerApi

def create_routes(api: Api):
    api.add_resource(UserApi,'/user/<user_id>')
    api.add_resource(UsersApi,'/users')
    api.add_resource(UserPaginateApi,'/user/paginate')
    api.add_resource(UbahPasswordApi,'/user/password/update/<user_id>')
    
    # api.add_resource(UserLoginApi,'/user/login')
    
    api.add_resource(CustomerProfileApi,'/customer/profile')
    api.add_resource(CustomerApi,'/customer')
    
    # api.add_resource(CustomerForgotPasswordApi,'/customer/forgot-password')
    api.add_resource(CustomerRegisterApi,'/customer/register')
    api.add_resource(CustomerLoginApi,'/customer/login')
    api.add_resource(CustomerAllApi,'/customer/all')
    
    
    
    # api.add_resource(StatusUserApi,'/status-user')
    api.add_resource(BusinessTypeApi,'/business-type')
    api.add_resource(BusinessTypePaginateApi,'/business-type/paginate')
    
    api.add_resource(GreetingApi,'/greeting')
    api.add_resource(GreetingPaginateApi,'/greeting/paginate')
    api.add_resource(InterestApi,'/interest')
    api.add_resource(InterestPaginateApi,'/interest/paginate')
    api.add_resource(ProfessionApi,'/profession')
    api.add_resource(ProfessionPaginateApi,'/profession/paginate')
    
    api.add_resource(ArticleAll,'/article/all')
    api.add_resource(ArticleApi,'/article')
    api.add_resource(ArticlePublicApi,'/article/public')
    api.add_resource(ArticlePublicCustomerApi,'/article/public/customer')
    
    api.add_resource(AffiliateApi,'/affiliate')
    api.add_resource(AffiliateSummaryApi,'/affiliate/summary')
    
    api.add_resource(ContactListSaveApi,'/contact/list-save')
    api.add_resource(ContactListShareApi,'/contact/list-share')
    api.add_resource(ContactApi,'/contact')
    api.add_resource(ContactSummaryApi,'/contact/summary')
    

    api.add_resource(CreditDatatablesApi,'/credit/datatables')
    api.add_resource(CreditApi,'/credit')
    api.add_resource(CreditBalanceCustomerApi,'/credit/balance/customer')
    api.add_resource(CreditBalanceApi,'/credit/balance')
    api.add_resource(CreditCustomerApi,'/credit/customer')
    api.add_resource(CreditTagApi,'/credit/<Tag>')

    api.add_resource(SettingsApi,'/settings')
    api.add_resource(SettingsPaginateApi,'/settings/paginate')
    
    api.add_resource(AccessTokenVerify,'/token/verify/<role_id>')
    api.add_resource(MenuApi,'/menu/<menu_id>')
    api.add_resource(MenusApi,'/menus')
    api.add_resource(MenuAllApi,'/menu-all')
    
    # api.add_resource(TokenApi,'/token')
    api.add_resource(RolesApi,'/role')
    api.add_resource(RoleApi,'/role/<role_id>')
    api.add_resource(RoleAllApi,'/role/all')

    # route role access
    api.add_resource(RolesAccessApi,'/role/access')
    api.add_resource(RoleAccessApi,'/role/access/<access_id>')
    api.add_resource(RoleAccessAllApi,'/role/access/all')
    
    api.add_resource(StartupApi,'/startup')
    api.add_resource(LogoutApi,'/logout/<role_id>')


    api.add_resource(UserLoginApi,'/admin/login')
    api.add_resource(ContactSaveApi,'/cjob/contact-save')
    api.add_resource(GetFile,'/article/getfile/<filename>')
    api.add_resource(GetArticle,'/article/<slug>')
    api.add_resource(FirebaseTokenApi,'/firebase')
    api.add_resource(NotificationApi,'/notification')
    api.add_resource(NotificationPaginateApi,'/notification/paginate')
    api.add_resource(NotificationSendApi,'/notification/send')
    
    api.add_resource(RequestWithdrawApi,'/withdraw/request')
    api.add_resource(WithdrawSummaryApi,'/withdraw/summary')
    api.add_resource(TotalAmountWithdrawApi,'/withdraw/amount')
    api.add_resource(WithdrawApi,'/withdraw')
    
    api.add_resource(BroadcastContactSaveApi,'/contact-sync/broadcast')
    api.add_resource(ContactDetailApi,'/contact/detail')
    
    api.add_resource(ProvinceApi,'/province')
    api.add_resource(ProvincePaginateApi,'/province/paginate')
    

    api.add_resource(CityApi,'/city')
    api.add_resource(CityListApi,'/city/list')
    api.add_resource(CityPaginateApi,'/city/paginate')

    api.add_resource(AffiliateKomisiApi,'/affiliate-komisi')
    api.add_resource(AffiliatePenghasilanApi,'/affiliate-penghasilan')

    api.add_resource(UpdateFotoApi,'/profile/upload/foto')
    api.add_resource(UpdateCoverApi,'/profile/upload/cover')
    api.add_resource(GetProfileImage,'/uploads/<dir_name>/<filename>')
    
    api.add_resource(RemoveFotoApi,'/profile/remove/foto')
    api.add_resource(RemoveCoverApi,'/profile/remove/cover')

    api.add_resource(UplineProfileApi,'/upline')
    api.add_resource(DownlineApi,'/downline')
    api.add_resource(AffiliatePenghasilanDetailApi,'/affiliate/penghasilan/detail')

    api.add_resource(DashboardApi,'/dashboard')
    
    # 
    api.add_resource(SaveContactManualApi,'/contact/save-manual')
    
    # Feed Admin
    api.add_resource(FeedAdmin,'/feed')
    api.add_resource(FeedAdminStatus,'/feed/status')

    # Feed Customer
    api.add_resource(FeedCustomer,'/feed-customer')

    # Feed Public 
    api.add_resource(GetFeed,'/feed/public/<FeedId>')
    api.add_resource(FeedPublicCustomerApi,'/feed/public/customer')
    api.add_resource(ListFeedPublicCustomerApi,'/feed/public/customer/<CustomerId>')
    api.add_resource(ClickAdsFeed,'/feed/click/<FeedId>')


    api.add_resource(SlidersApi,'/sliders')
    api.add_resource(SliderApi,'/slider/<slider_id>')
    api.add_resource(SliderCustomerApi,'/slider/customer')
    