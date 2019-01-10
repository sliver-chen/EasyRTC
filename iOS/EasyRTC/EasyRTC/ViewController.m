//
//  ViewController.m
//  EasyRTC
//
//  Created by cx on 2018/12/10.
//  Copyright © 2018 cx. All rights reserved.
//

#import "ViewController.h"
#import "EasyLog.h"
#import "SocketWraper.h"
#import "CallViewController.h"

#pragma clang diagnostic ignored "-Wdeprecated-declarations"

#define TAG "ViewController"

@interface ViewController ()<SocketDelegate, UITableViewDelegate, UITableViewDataSource, UIAlertViewDelegate>

@property (weak, nonatomic) IBOutlet UITableView *AgentsView;

@property (weak, nonatomic) IBOutlet UIButton *CallButton;

@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *CallProcessView;

@property (strong, nonatomic) UIAlertView *alertView;

@property (strong, nonatomic) NSArray *userAgentList;

@property (strong, nonatomic) NSString *actionType;

@property (strong, nonatomic) NSString *chooseAgentID;

@property (strong, nonatomic) NSString *chooseAgentType;

@end

@implementation ViewController

#pragma mark ViewController delegate
- (void)viewDidLoad
{
    [super viewDidLoad];
    EasyLog(TAG, "ViewController did load");
    self.alertView = [[UIAlertView alloc]initWithTitle:@"收到呼叫" message:@"" delegate:self cancelButtonTitle:@"挂断" otherButtonTitles:@"接听", nil];
    
    self.userAgentList = [[NSMutableArray alloc]init];
    self.AgentsView.dataSource = self;

    [[SocketWraper shareSocketWraper] addListener:self];
    [[SocketWraper shareSocketWraper] requestUserList];
}

- (void)viewDidDisappear:(BOOL)animated
{
    EasyLog(TAG, "ViewController did disapper");
    [[SocketWraper shareSocketWraper] removeListener:self];
    [super viewDidDisappear:animated];
}

- (IBAction)CallButtonClicked:(id)sender
{
    EasyLog(TAG, "Call Button Clicked");

    if ([self getChooseAgent]) {
        [SocketWraper shareSocketWraper].target = self.chooseAgentID;
        NSString *target = [SocketWraper shareSocketWraper].target;
        NSString *source = [SocketWraper shareSocketWraper].uid;

        if ([target isEqualToString:source] || target == nil) {
            EasyLog(TAG, "can't call to yourself");
            return;
        }
        [self.CallProcessView startAnimating];
        [[SocketWraper shareSocketWraper] emit:@"invite" value:@"yes"];
    }
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    CallViewController *next = (CallViewController *)segue.destinationViewController;
    next.actionType = [self.actionType mutableCopy];
    next.ifNeedAddStream = true;

    if ([self.chooseAgentType isEqualToString:@"Android_Camera"]) {
        next.ifNeedAddStream = false;
    }
}

#pragma mark socket delegate
- (void)onUserAgentsUpdate:(NSArray *)data
{
    self.userAgentList = data;
    [self.AgentsView reloadData];
    [self setChooseAgent:0];
}

- (void)onRemoteEventMsg:(NSString *)source target:(NSString *)target
                    type:(NSString *)type value:(NSString *)value
{
    [self processSignal:source target:target type:type value:value];
}

- (void)onRemoteCandidate:(int)label mid:(NSString *)mid candidate:(NSString *)candidate
{
    
}

#pragma mark tableview delegate
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.userAgentList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell = [[UITableViewCell alloc] init];
    NSDictionary *dict = self.userAgentList[indexPath.row];
    cell.textLabel.text = dict[@"name"];
    return cell;
}

#pragma mark alertview delegate
- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0) {
        EasyLog(TAG, "选择挂断");
    } else if (buttonIndex == 1) {
        EasyLog(TAG, "选择接听");
        [[SocketWraper shareSocketWraper] emit:@"ack" value:@"yes"];
        self.actionType = @"answer";
        [self performSegueWithIdentifier:@"callSegue" sender:nil];
    } else {
        EasyLog(TAG, "未知按键");
    }
}

#pragma mark common method
- (void)setChooseAgent:(int)idx
{
    NSIndexPath *index = [NSIndexPath indexPathForRow:0 inSection:0];
    [self.AgentsView selectRowAtIndexPath:index animated:NO scrollPosition:UITableViewScrollPositionNone];
}

- (bool)getChooseAgent
{
    NSIndexPath *index = [self.AgentsView indexPathForSelectedRow];
    if (index) {
        NSDictionary *dict = self.userAgentList[index.row];
        self.chooseAgentID = dict[@"id"];
        self.chooseAgentType = dict[@"type"];
        return true;
    }
    
    return false;
}

- (void)processSignal:(NSString *)source target:(NSString *)target
                 type:(NSString *)type value:(NSString *)value
{
    if ([target isEqualToString:[SocketWraper shareSocketWraper].uid]) {
        if ([type isEqualToString:@"invite"]) {
            [SocketWraper shareSocketWraper].target = source;
            [self.alertView show];
        } else if ([type isEqualToString:@"ack"]) {
            [self.CallProcessView stopAnimating];
            if ([value isEqualToString:@"yes"]) {
                EasyLog(TAG, "agree to call");
                self.actionType = @"offer";
                [self performSegueWithIdentifier:@"callSegue" sender:nil];
            } else {
                EasyLog(TAG, "refuse to call");
            }
        }
    } else {
        EasyLog(TAG, "get error target");
    }
}

@end
